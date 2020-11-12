#!/usr/bin/python3
import sys
import matplotlib.pyplot as plt
import numpy as np

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

# values are a list two dictionaries dictionaries followed by name
#[dictionary w/ key = timestamp val = genre, [sum of weights, dictionary w/ key = genre val = weight], artist name]
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

        artists[arid] = [dict(),[0.0,dict()], name]
        current_artist = arid;
    # value row
    else:
        cells = line.split(",")
        genre = cells[0]
        timestamp = int(cells[1])
        if arid is None:
            print("First line was not an artist, error reading file.")
            exit()
        else:
            artists[arid][0][timestamp] = genre

# at this point the whole file is read
file.close()

# add up the weights
for key in artists.keys():
    timestamp_list = list(artists[key][0].keys())
    if len(timestamp_list) == 0:
        continue

    time_weight_list = generate_time_weights(len(timestamp_list))

    i = 0
    for timestamp in timestamp_list:
        time_weight = time_weight_list[i]
        genre = artists[key][0][timestamp]

        if genre in artists[key][1][1]:
            artists[key][1][1][genre] = artists[key][1][1][genre] + time_weight
        else:
            artists[key][1][1][genre] = float(time_weight)

        artists[key][1][0] = artists[key][1][0] + float(time_weight) # add to the sum

        i = i + 1

# normalize the weights
for key in artists.keys():
    for genre in artists[key][1][1]:
        artists[key][1][1][genre] = artists[key][1][1][genre] / artists[key][1][0]

# for TESTING only, print all artists
for arid in artists:
   print(str(arid)+"  ---  "+artists[arid][2])
   for genre in artists[arid][1][1]:
       weight = artists[arid][1][1][genre]
       print(genre+"  ---  "+str(weight))
   print()


if len(sys.argv) == 1:
    tid = int(sys.argv[1])
else:
    tid = int(input("'arid' of artist to compare against: "))
    print()

print(str(tid)+"  ---  "+artists[tid][2])
for genre in artists[tid][1][1]:
    weight = artists[tid][1][1][genre]
    print(genre+"  ---  "+str(weight))
    i = i + 1

# key of delta, value of arid
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
    deltas[delta] = arid

# TESTING print deltas
for delta in sorted(deltas.keys()):
    print(str(deltas[delta])+"  --  "+str(delta))

# graph bar graph
x_lab = artists[tid][1][1].keys()
x = np.arange(len(x_lab))
y = artists[tid][1][1].values()
y1 = []
y2 = []
d1 = sorted(deltas.keys())[0]
d2 = sorted(deltas.keys())[1]
a1 = deltas[d1]
a2 = deltas[d2]
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
ax.bar(x-w, y, width=w, align='center', label=str(tid))
ax.bar(x, y1, width=w, align='center', label=str(a1)+" - delta: "+str(d1))
ax.bar(x+w, y2, width=w, align='center', label=str(a2)+" - delta: "+str(d2))
plt.legend(loc="upper left")
plt.show()
