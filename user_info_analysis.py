# -*- coding: utf-8 -*-
"""
Created on Fri Mar 20 13:54:24 2020

@author: vikrant.sahu
"""

import pymongo
import pandas as pd
pd.set_option('display.max_columns', None)


from pymongo import MongoClient

import datetime as dt
from dateutil.parser import parse

from datetime import *
from dfply import *


#point the client at mongo URL
client = MongoClient('192.168.100.180:27017', username='git_analytics',
                                                password='dspg@007',)
#select database
db = client['Github-App-temp']

#http://gitserver.digite.in/SALM/AI/automl/blob/master/com/utility/database/MongoDBUtils.py

# =============================================================================
# cursor = db.issues_info_all.find({
#   "repo_id": "31792824" 
#    ,"issue_created_at": { "$gt": (1582482600000), "$lt": (1584901799999)}
#  })
# print(list(cursor))
# =============================================================================


####Load all the collections######

##user details
user_details_list = db["user_info"]
user_details_df = pd.DataFrame(list(user_details_list.find()))

##user_issues
user_issues_list = db["issues_user_bulk"]
user_issues_df = pd.DataFrame(list(user_issues_list.find()))

##user_pulls
user_pulls_list = db["pulls_user_bulk"]
user_pulls_df = pd.DataFrame(list(user_pulls_list.find()))

##user_repos
user_repos_list = db["repos_user_bulk"]
user_repos_df = pd.DataFrame(list(user_repos_list.find()))

##push_users
user_pushs_list = db["push_user"]
user_pushs_df = pd.DataFrame(list(user_pushs_list.find()))

###Querry for last 4 weeks commits
 
todays_date = dt.date.today()
date_weekday = todays_date.weekday()

start_date = todays_date - dt.timedelta(days=(21 + date_weekday))

##datetime to epoch:
start_date_ep = int(parse(start_date.strftime("%m/%d/%Y")).timestamp()*1000)

start_date_ep
end_date_ep
## selected user_id = 551196
###query
query_cursor = db.issues_info_all.find({
  "issuer_id": "551196"   #{"user_id": {$in : ["79138"]}}
   ,"issue_created_at": { "$gt": (158369220 0000), "$lt": (1585593000000)}
 })

## epoch to datetime
# =============================================================================
# epoch = (1241006778000 / 1000)
# dt = time.strftime("%d %m %Y %H:%M", time.gmtime(epoch))
# 
# =============================================================================


##Operation on DFs to calcualte various metrics####

##Get the max and min of user pulls
max_pull_epoch = user_pulls_df.pull_created_at.max()
min_pull_epoch = user_pulls_df.pull_created_at.min()


datetime.utcfromtimestamp(max_pull_epoch/1000).strftime('%Y-%m-%d %H:%M:%S')
datetime.utcfromtimestamp(min_pull_epoch/1000).strftime('%Y-%m-%d %H:%M:%S')
1585409994000

##Get the unique user for pulls, push and issues##
user_issues_df >> mask(X.state == "closed", X.assignees_logins == "[]") >> select(X.assignees_logins)

#>> select(X.assignees_logins)



##Create a new collection
temp_collection = db["temp_collection"]

####push the dataframe to mongo ####
import json

###remove the _id created by mongo db
df_to_push = user_details_df.drop("_id", axis=1)

###convert the df to json
records = json.loads(df_to_push.T.to_json()).values()

##insert into mongo
db.temp_collection.insert_many(records)

###Essential mongoDB operations###

##Querry to update the value
collection = db['installation_info']
        query = {"installation_id": str(df['installation.id'][0])}
        collection.update_many(query,{'$set':{'installation_status':'deleted'}})

##Querry to update the value
collection = db['user_info']
        query = {"user_id": "18552651"}
        collection.update_many(query,{'$set':{'metric_calc_status':'done'}})

##for a user id  and highest of week_start_date
query_cursor = db.summarized_weekly.find({
  "user_id": "79138" }).sort("week_start_date", -1).limit(1)

##delete a record with condition
collection = db['user_info']
        query = {"user_id": "18552651", }
        collection.delete_many(query)


####add a column to a mongo collection#####
##user details
user_details_list = db["user_info"]
user_details_df = pd.DataFrame(list(user_details_list.find()))

user_details_df = user_details_df.drop(["_id"], axis = 1)

##create two columns
user_details_df["last_refresh"] = 0  
user_details_df["abt_calculated"] = "NA"

###convert the df to json
import json
records = json.loads(user_details_df.T.to_json()).values()

##insert into mongo
db.user_info.insert_many(records)




###get user id for a user_name
user_login = "jonahwilliams"

collection = db['user_info']
querry = {"user_name": user_login}
cursor = collection.find(querry)
user_id = pd.DataFrame(list(cursor))['user_id'][0]

#Function to delte a list of folders
def delete_repo(path_list):
      
      for path in path_list:
            os.rmdir(path)
            
            
installation_id = "7853227"

collection = db['installation_info']
querry = {"installation_id": installation_id}
cursor = collection.find(querry)
path_list = pd.DataFrame(list(cursor))['repo_cloned_path']

delete_repo(path_list)

for path in path_list:
      print(path)
      print(type(path))

##String to date 
 now_utc = datetime.strptime(now_utc, '%d-%m-%Y')
 #https://www.tutorialspoint.com/How-do-I-convert-a-datetime-to-a-UTC-timestamp-in-Python
#########
      [day.date()
                  for day in rrule(MONTHLY
                     , dtstart = date(first_month_start.year,first_month_start.month,first_month_start.day)
                     , until = last_month_end)]

wk_list = pd.date_range(start= created_at_utc, end=now_utc, 
                         freq='W-MON').strftime('%d-%m-%Y').tolist()

#########
pr_user >> select(X.additions, X.pull_merged_at, X.deletions) >> group_by(X.pull_merged_at) >> summarize_each(sum)

cols = ["additions", "pull_merged_at"]
summarise_opp = "n"
pr_user >> select(X[cols]) >> group_by(X.pull_merged_at) >> summarize_each([eval(summarise_opp)], X.additions)


##Test
issuer_id = "4823303"
user_id = "4823303"
load_data(db,'issues_user_bulk',{'issuer_id':issuer_id})
[s + mystring for s in mylist]

# =============================================================================
#             summarised_df = (df_subset >> group_by(X.grpBy_col) >> summarize_each([eval(summarise_opertn)]
#                                                       , X[summarise_cols]  ))
# =============================================================================
# =============================================================================
# #####testing
# df = pr_user
# time_freq = "week"
# date_col = "pull_merged_at"
# summarise_cols = ["commits", "changed_files"]
# summarise_cols_names = ["total_commits", "files_modified"]
# summarise_opertn = "np.sum"
# cols_list = ["X." + s  for s in summarise_cols]
# 
# str(["X." + s  for s in summarise_cols])[1:-1]
# 
# eval(str(["X." + s  for s in summarise_cols])[1:-1])
# 
# map(eval, cols_list)
# 
# [eval(i) for i in cols_list]
#             
# X.commits, X.changed_files
# 
# ##########
# xx = df_subset.groupby("grpBy_col").sum()[summarise_cols].reset_index()
# 
# summarised_df.rename(
#     columns={i:j for i,j in zip(summarise_cols,summarise_cols_names)}, inplace=True
# )
# =============================================================================
user_id = "10163662" #"4823303" 
collection = db['summarized_monthly']
querry = {"user_id": user_id}
cursor = collection.find(querry)
df_2 = pd.DataFrame(list(cursor))


df_3 = load_data(db = db, collection_name = "installation_info", query = {})
