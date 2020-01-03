#import libraries
import pandas as pd
import matplotlib.pyplot as plt
import seaborn as sns
import sys

print(sys.argv[2])

#read data from csv file
airbnb = pd.read_csv(sys.argv[2])

#show first five rows of record
airbnb.head(5)

#create count plot for room types
sns.set(style="darkgrid")
ax = sns.countplot(x='room_type', hue="neighbourhood_group", data=airbnb)
plt.savefig('/tmp/count_of_room_type_by_neighbourhood.png')