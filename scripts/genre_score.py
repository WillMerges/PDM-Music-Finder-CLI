#!/usr/bin/python3
import sys
import matplotlib.pyplot as plt

# TODO
# for now, use a linear weighting (maybe make logistic?)
def generate_time_weights(N):
    ret = []
    delta = 100.0 / N
    curr = 100.0
    for i in range(N):
        ret.append(curr)
        curr = curr - delta

    return ret


if len(sys.argv) != 2:
    print("run as: genre_score.py <filename>")
    exit()

filename = sys.argv[1]

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

#for testing only
for arid in artists:
   print(str(arid)+"  ---  "+artists[arid][2])
   for genre in artists[arid][1][1]:
       weight = artists[arid][1][1][genre]
       print(genre+"  ---  "+str(weight))
   print()


if len(sys.argv) == 3:
    arid = int(sys.argv[2])
else:
    arid = int(input("'arid' of artist to compare against: "))
    print()

print(str(arid)+"  ---  "+artists[arid][2])
for genre in artists[arid][1][1]:
    weight = artists[arid][1][1][genre]
    print(genre+"  ---  "+str(weight))
    i = i + 1

genres = artists[arid][1][1].keys()
weights = artists[arid][1][1].values()
fig = plt.figure(figsize = (10, 5))
plt.bar(genres, weights, width=0.95, color='orange')

plt.show()
