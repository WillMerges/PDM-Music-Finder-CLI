#!/usr/bin/python3
import sys
import matplotlib.pyplot as plt
import numpy as np

# value multiplied by % played to get weight
playcount_multiplier = 100.0

# for now, use a linear weighting (maybe make logistic?)
def generate_time_weights(N):
    ret = []
    delta = 100.0 / N
    curr = 100.0
    for i in range(N):
        ret.append(curr)
        curr = curr - delta

    return ret

filename = "../artist_genre_scores.csv"

# keys are arid's values are a list two dictionaries dictionaries followed by name
#[dictionary w/ key = timestamp val = [genre, playcount], [sum of weights, dictionary w/ key = genre val = weight], artist name, total song plays]
artists = dict()

file = open(filename)

reading = True
current_artist = None
while reading:
    line = file.readline()

    if not line:
        reading = False
        continue

    # new artist
    if line.find(",") == -1:
        line = line.replace("\006", ",") # change the /006 character to a comma since it had to be changed for CSV format
        info = line.split(":", 2) # split into two tokens
        arid = int(info[0])
        name = info[1][:-1]

        artists[arid] = [dict(),[0.0,dict()], name, 0]
        current_artist = arid;
    # value row
    else:
        cells = line.split(",")
        genre = cells[0]
        timestamp = int(cells[1])
        playcount = int(cells[2])
        if current_artist is None:
            print("First line was not an artist, error reading file.")
            exit()
        else:
            artists[current_artist][0][timestamp] = [genre, playcount]
            artists[current_artist][3] = artists[current_artist][3] + playcount

# at this point the whole file is read
file.close()

# add up the weights
for key in artists.keys():
    timestamp_list = list(artists[key][0].keys())
    if len(timestamp_list) == 0:
        continue

    total_plays = float(artists[key][3])
    time_weight_list = generate_time_weights(len(timestamp_list))

    i = 0
    for timestamp in timestamp_list:
        time_weight = time_weight_list[i]
        genre = artists[key][0][timestamp][0]
        playcount = artists[key][0][timestamp][1]
        play_weight = float(playcount) / total_plays * playcount_multiplier

        if genre in artists[key][1][1]:
            artists[key][1][1][genre] = artists[key][1][1][genre] + time_weight + play_weight
        else:
            artists[key][1][1][genre] = float(time_weight) + play_weight

        artists[key][1][0] = artists[key][1][0] + float(time_weight) + play_weight # add to the sum

        i = i + 1

# normalize the weights
for key in artists.keys():
    for genre in artists[key][1][1]:
        artists[key][1][1][genre] = artists[key][1][1][genre] / artists[key][1][0]

# for TESTING only, print all artists
for arid in sorted(list(artists.keys())):
   print(str(arid)+"  ---  "+artists[arid][2])
   for genre in artists[arid][1][1]:
       weight = artists[arid][1][1][genre]
       print(genre+"  ---  "+str(weight))
   print()


if len(sys.argv) == 2:
    tid = int(sys.argv[1])
else:
    tid = int(input("'arid' of artist to compare against: "))
    print()

print(str(tid)+"  ---  "+artists[tid][2])
for genre in artists[tid][1][1]:
    weight = artists[tid][1][1][genre]
    print(genre+"  ---  "+str(weight))
    i = i + 1

# key of delta, value of list of arid's
deltas = dict()

for arid in artists:
    if arid == tid:
        continue
    delta = 0
    for genre in artists[tid][1][1]:
        if genre in artists[arid][1][1]:
            delta = delta + abs(artists[tid][1][1][genre] - artists[arid][1][1][genre])
        else: #penalize for not having that genre
            delta = delta + artists[tid][1][1][genre]

    if delta not in deltas:
        deltas[delta] = [arid]
    else:
        deltas[delta].append(arid)

# # TESTING print deltas
# for delta in sorted(deltas.keys()):
#     for arid in deltas[delta]:
#         print(str(arid)+"  --  "+str(delta))

# graph bar graph
x_lab = artists[tid][1][1].keys()
x = np.arange(len(x_lab))
y = artists[tid][1][1].values()
y1 = []
y2 = []
d = sorted(deltas.keys())[0]
if len(deltas[d]) > 1:
    a1 = deltas[d][0]
    a2 = deltas[d][1]
    d1 = d2 = d
else:
    d1 = d
    a1 = deltas[d][0]
    d2 = sorted(deltas.keys())[1]
    a2 = deltas[d2][0]
name = artists[tid][2]
name1 = artists[a1][2]
name2 = artists[a2][2]
for genre in x_lab:
    if genre in artists[a1][1][1]:
        y1.insert(0, artists[a1][1][1][genre])
    else:
        y1.insert(0, 0.0)

    if genre in artists[a2][1][1]:
        y2.insert(0, artists[a2][1][1][genre])
    else:
        y2.insert(0, 0.0)

plt.style.use('bmh')
ax = plt.subplot(111)
w = 0.1
plt.xticks(x, x_lab)
plt.ylabel("Normalized Weight")
plt.xlabel("Genre")
ax.bar(x-w, y, width=w, align='center', label=name + " (" + str(tid) + ")")
ax.bar(x, y1, width=w, align='center', label=name1 + " (" + str(a1) + ") - delta: " + str(d1))
ax.bar(x+w, y2, width=w, align='center', label=name2 + " (" +str(a2)+ ") - delta: " + str(d2))
plt.legend(loc="upper left")
plt.show()
