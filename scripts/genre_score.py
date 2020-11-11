#!/usr/bin/python3
import sys

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

# values are a list of dictionaries
#[dictionary w/ key = timestamp val = genre, [sum of weights, dictionary w/ key = genre val = weight]]
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
        arid = int(line)
        artists[arid] = [dict(),[0.0,dict()]]
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
    print(str(arid)+":")
    for genre in artists[arid][1][1]:
        weight = artists[arid][1][1][genre]
        print(genre+"  ---  "+str(weight))

file.close()
