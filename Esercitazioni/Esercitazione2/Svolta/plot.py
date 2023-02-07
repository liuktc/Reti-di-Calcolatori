import matplotlib.pyplot as plt
import numpy as np
import csv
import math

t = []
s = []
with open('data.csv') as csv_file:
    csv_reader = csv.reader(csv_file, delimiter=',')
    line_count = 0
    for row in csv_reader:
        s.append(int(row[1]))
        t.append(math.log2(int(row[0])))
        print(str(int(row[1])) + " | " + str(math.log2(int(row[0]))))
# Data for plotting
fig, ax = plt.subplots()
ax.scatter(t, s)

ax.set(xlabel='Log2(n)', ylabel='Time (ms)',
       title='Tempo di trasferimento di un file di 210MB con n byte per messaggio');
ax.grid()
ax.set_xticks(np.arange(10,21,1))
fig.savefig("test.png",dpi=600)
plt.show()