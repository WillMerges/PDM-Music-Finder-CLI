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

filename = "../user_genre_scores.csv"

# keys are usernames values are:
#[dictionary w/ key = timestamp val = list of [genre, playcount], [sum of weights, dictionary w/ key = genre val = weight], total entries, total song plays]
users = dict()

file = open(filename)

reading = True
current_user = None
while reading:
    line = file.readline()

    if not line:
        reading = False
        continue

    # new user
    if line.find(",") == -1:
        line = line.replace("\006", ",")[:-1].strip() # change the /006 character to a comma since it had to be changed for CSV format
        #info = line.split(":", 2) # split into two tokens
        #arid = int(info[0])
        #name = info[1][:-1]

        if line == "":
            continue

        users[line] = [dict(),[0.0,dict()], 0, 0]
        current_user = line;
    # value row
    else:
        cells = line.split(",")
        genre = cells[0]
        timestamp = int(cells[1])
        playcount = int(cells[2])
        if current_user is None:
            print("First line was not an user, error reading file.")
            exit()
        else:
            if timestamp in users[current_user][0]:
                users[current_user][0][timestamp].append([genre, playcount])
            else:
                users[current_user][0][timestamp] = [[genre, playcount]]

            users[current_user][2] = users[current_user][2] + 1
            users[current_user][3] = users[current_user][3] + playcount

# at this point the whole file is read
file.close()

# # TESTING
# for user in users:
#     print("-------------------------------")
#     print(user)
#     for t in users[user][0]:
#         print(t)
#         for entry in users[user][0][t]:
#             print(entry[0]+" "+str(entry[1]))
#     print("-------------------------------")
#     print()

# add up the weights
for key in users.keys():
    timestamp_list = list(users[key][0].keys())
    if len(timestamp_list) == 0:
        continue

    total_plays = float(users[key][3])

    N = users[key][2]
    time_weight_list = generate_time_weights(N)

    i = 0
    for timestamp in timestamp_list:
        for entry in users[key][0][timestamp]: #this is a list
            time_weight = time_weight_list[i]

            genre = entry[0]
            playcount = entry[1]
            play_weight = float(playcount) / total_plays * playcount_multiplier

            if genre in users[key][1][1]:
                users[key][1][1][genre] = users[key][1][1][genre] + time_weight + play_weight
            else:
                users[key][1][1][genre] = float(time_weight) + play_weight

            users[key][1][0] = users[key][1][0] + float(time_weight) + play_weight # add to the sum

            i = i + 1

# normalize the weights
for key in users.keys():
    for genre in users[key][1][1]:
        users[key][1][1][genre] = users[key][1][1][genre] / users[key][1][0]

# for TESTING only, print all users
for arid in list(users.keys()):
   print(arid)
   for genre in users[arid][1][1]:
       weight = users[arid][1][1][genre]
       print(genre+"  ---  "+str(weight))
   print()


if len(sys.argv) == 2:
    tid = sys.argv[1]
else:
    tid = input("username of user to compare against: ")
    print()

print(tid)
for genre in users[tid][1][1]:
    weight = users[tid][1][1][genre]
    print(genre+"  ---  "+str(weight))
    i = i + 1

# key of delta, value of list of arid's
deltas = dict()

for arid in users:
    if arid == tid:
        continue
    delta = 0
    for genre in users[tid][1][1]:
        if genre in users[arid][1][1]:
            delta = delta + abs(users[tid][1][1][genre] - users[arid][1][1][genre])
        else: #penalize for not having that genre
            delta = delta + users[tid][1][1][genre]

    if delta not in deltas:
        deltas[delta] = [arid]
    else:
        deltas[delta].append(arid)

# # TESTING print deltas
# for delta in sorted(deltas.keys()):
#     for arid in deltas[delta]:
#         print(str(arid)+"  --  "+str(delta))

# graph bar graph
x_lab = users[tid][1][1].keys()
x = np.arange(len(x_lab))
y = users[tid][1][1].values()
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
for genre in x_lab:
    if genre in users[a1][1][1]:
        y1.insert(0, users[a1][1][1][genre])
    else:
        y1.insert(0, 0.0)

    if genre in users[a2][1][1]:
        y2.insert(0, users[a2][1][1][genre])
    else:
        y2.insert(0, 0.0)

plt.style.use('bmh')
ax = plt.subplot(111)
w = 0.1
plt.xticks(x, x_lab)
plt.ylabel("Normalized Weight")
plt.xlabel("Genre")
ax.bar(x-w, y, width=w, align='center', label=tid)
ax.bar(x, y1, width=w, align='center', label=a1+" - delta: "+str(d1))
ax.bar(x+w, y2, width=w, align='center', label=a2+" - delta: "+str(d2))
plt.legend(loc="upper left")
plt.show()
