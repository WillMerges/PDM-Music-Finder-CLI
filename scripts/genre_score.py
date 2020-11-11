#!/usr/bin/python3

import sys

if len(sys.argv) != 2:
    print("run as: genre_score.py <filename>")
    exit()

filename = sys.argv[1]

#values are dictionary w/ keys of timestamps and values of genres
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
        artists[arid] = dict()
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
            artists[arid][timestamp] = genre

# at this point the whole file is read

file.close()
