#!/usr/bin/env python
# coding: utf-8


#this code is written in python 3.7
#you need to run pip install pandas

import pandas as pd

#create a dict with column names and values
data = {
    'name': ['June', 'Robert', 'Lily', 'David'],
    'apples': [3, 2, 0, 1], 
    'oranges': [0, 3, 7, 2]
}

#create a pandas data frame from the dict
purchases = pd.DataFrame(data)

#save the file to a CSV
purchases.to_csv('/tmp/new_purchases.csv', index = False)
#purchases.to_csv(sys.stdout, index = False)

#or JSON
purchases.to_json('new_purchases.json')

#print(purchases.head())
