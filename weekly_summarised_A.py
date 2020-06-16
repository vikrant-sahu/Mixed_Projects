# -*- coding: utf-8 -*-
"""
Created on Mon Apr 13 15:43:45 2020

@author: amoolya.shetty
"""
import pymongo
import pandas as pd
import time as t
from dfply import * 
pd.set_option('display.max_columns', None)
from pymongo import MongoClient
from datetime import datetime
import datetime as dt
from dateutil.parser import parse



#---------------------------------------------------------------------------------------
# function definition section

# function to convert the epoch values to UTC date format
def weekStartInUtc(utcDate):
    utcDateWeekday = utcDate.weekday()
    weekStart = utcDate - dt.timedelta(utcDateWeekday)
    return(weekStart)
    
def weekEndInUtc(utcDate):
     utcDateWeekday = utcDate.weekday()
     weekEnd = utcDate + dt.timedelta(7-utcDateWeekday)
     return(weekEnd)
     
# function to convert epoch to UTC 
def convertEpochToUtc(epochTime):
    dateString = datetime.utcfromtimestamp(epochTime/1000).strftime('%Y-%m-%d')
    dateUtc = datetime.strptime(dateString, "%Y-%m-%d")
    return (dateUtc)

# function to convert UTC to Epoch
def convertUtcToEpoch(utcTime):
    return (int(parse(utcTime.strftime("%m/%d/%Y")).timestamp()*1000))

# function to find intermediate weeks week start date in UTC
def findIntermediateDurationStart(firstWeekStartEp,lastWeekEndEp,baseEpoch):
    #baseEpoch for week is 604800000
    intermediateWeekStartSeries = pd.Series (range(firstWeekStartEp,lastWeekEndEp,baseEpoch))
    return(intermediateWeekStartSeries)
    
# function to calculate user Account Time Frame - create date up until current date
def userTimeFrame(userId):
    #extract a specific user info
    specificUserInfo = userDataDf[userDataDf['user_id'] == userId];
    # extract the date when the user account was created
    createdAtEp = specificUserInfo['created_at'];
    # convert epoch to UTC
    createdAtUtc = convertEpochToUtc(createdAtEp);
    # find the week's start date In UTC 
    firstWeekStart = weekStartInUtc(createdAtUtc);
    firstWeekStartEp = convertUtcToEpoch(firstWeekStart);  
    # obtain current date
    currentDateUtc = dt.date.today();
    # find week's end date in UTC
    lastWeekEndUtc = weekEndInUtc(currentDateUtc);
    lastWeekEndEp = convertUtcToEpoch(lastWeekEndUtc);
    timeFrame=[firstWeekStartEp,lastWeekEndEp];
    return(timeFrame)
 

#------------------------------------------------------------------------------------------------
#function to perform data filteration on any collection for a given user TimeFrame
    specificUserPullInfo = collectionDf[collectionDf[collectionUserFieldName] == userId];
    specificUserPullInfo['count'] = 1;
    # if pull_merged _at is NAN,replace it with 0
    specificUserPullInfo[operationFieldName] = specificUserPullInfo[operationFieldName].fillna(0);
    filteredUserData = specificUserPullInfo[specificUserPullInfo[operationFieldName]>firstDurationStartEp ]
    filteredUserData['time_utc'] = filteredUserData[operationFieldName].apply(convertEpochToUtc);
    filteredUserData['week_start_utc'] = filteredUserData['time_utc'].apply(weekStartInUtc);
#    groupByResultDf = (filteredUserData >>
#        group_by(X.week_start_utc)>>
#         summarize(totalCommits = X.commits.sum())
#      )
    return (filteredUserData)

#--------------------------------------------------------------------------------------------------

#point the client at mongo URI
client = MongoClient('192.168.100.180:27017', username='git_analytics',
                                                password='dspg@007',)
#select database Github-App-temp from mongoDb
#client.list_database_names()
db = client['Github-App-temp']

#select collection that holds user info
userData =  db['user_info']
userDataDf = pd.DataFrame(list(userData.find()))

#select collection that holds user user pull information
pullsUserBulk =  db['pulls_user_bulk']
pullsUserBulkDf = pd.DataFrame(list(pullsUserBulk.find()));

#select collection that holds user user pull information
issuesUserBulk =  db['issues_user_bulk']
issuesUserBulkDf = pd.DataFrame(list(issuesUserBulk.find()));

#for col in issuesUserBulkDf.columns:
#    print(col)


# pull required info for a specific user
userId= "79138";
# baseEpoch is epach value for a a given duration i.e weekly or monthy
# baseEpoch for a week is 604800000
baseEpoch = 604800000 ;

# filter out the start(account created) and end time (current date) for a user
timeFrame  = userTimeFrame(userId);
firstDurationStartEp = timeFrame[0]
lastDurationEndEp = timeFrame[1]
#create a dataframe that holds weekly summarized information
weeklySummarizedDf = summarizedTable(userId,firstDurationStartEp,lastDurationEndEp,baseEpoch);

# filter out information from a pull_user_bulk collection for a specific user
collectionDf = pullsUserBulkDf ;
collectionUserFieldName = 'pull_requester_id' ;
operationFieldName = 'pull_merged_at';
filteredUserData = tableOperation(collectionDf,collectionUserFieldName,userId,operationFieldName,firstDurationStartEp,lastDurationEndEp);
#-----------------------------------------------------------------------------------------------------------------------------------
# find total commits per week for a user
groupByResultCommitsDf = (filteredUserData >>
        group_by(X.week_start_utc)>>
         summarize(totalCommits = X.commits.sum())
      );

result = pd.merge(weeklySummarizedDf, groupByResultCommitsDf, how='left',  on=['week_start_utc']);
#------------------------------------------------------------------------------------------------------------
# find the weeks that have number of additions made by the user above 300 LOC
groupByResultAdditionsDf = (filteredUserData >>
        group_by(X.week_start_utc)>>
         summarize(totalMajorAdditions = X.additions.sum())
      );
groupByResultAdditionsDf['totalMajorAdditions'] = groupByResultAdditionsDf['totalMajorAdditions'].apply( lambda x : pd.to_numeric(x))
groupByMajorAdditionDf= groupByResultAdditionsDf[groupByResultAdditionsDf ['totalMajorAdditions'] >300]
result1 = pd.merge(result, groupByMajorAdditionDf, how='left',  on=['week_start_utc']);
#------------------------------------------------------------------------------------------=------------
# find the weeks that have number of additions made by the user below 300 LOC
        group_by(X.week_start_utc)>>
         summarize(totalMinorAdditions = X.additions.sum())
      );
groupByResultAdditionsDf['totalMinorAdditions'] = groupByResultAdditionsDf['totalMinorAdditions'].apply( lambda x : pd.to_numeric(x))
groupByMinorAdditionDf= groupByResultAdditionsDf[groupByResultAdditionsDf ['totalMinorAdditions'] <300]
result2 = pd.merge(result1, groupByMinorAdditionDf, how='left',  on=['week_start_utc']);

#--------------------------------------------------------------------------------------
# find the weeks that have number of deletions made by the user above 300 LOC
groupByResultDeletionDf = (filteredUserData >>
        group_by(X.week_start_utc)>>
         summarize(totalMajorDeletions = X.deletions.sum())
      );

groupByResultDeletionDf['totalMajorDeletions'] = groupByResultDeletionDf['totalMajorDeletions'].apply( lambda x : pd.to_numeric(x))
groupByMajorDeletionDf= groupByResultDeletionDf[groupByResultDeletionDf ['totalMajorDeletions'] >300]
result3 = pd.merge(result2, groupByMajorDeletionDf, how='left',  on=['week_start_utc']);

#-------------------------------------------------
# find the weeks that have number of deletions made by the user below 300 LOC
groupByResultDeletionDf = (filteredUserData >>
        group_by(X.week_start_utc)>>
         summarize(totalMinorDeletions = X.deletions.sum())
      );

groupByResultDeletionDf['totalMinorDeletions'] = groupByResultDeletionDf['totalMinorDeletions'].apply( lambda x : pd.to_numeric(x))
groupByMinorDeletionDf= groupByResultDeletionDf[groupByResultDeletionDf ['totalMinorDeletions'] <300]
result4 = pd.merge(result3, groupByMinorDeletionDf, how='left',  on=['week_start_utc']);
#-------------------------------------------------
# find total files changed  in a week by a user
groupByResultChangeFilesDf = (filteredUserData >>
        group_by(X.week_start_utc)>>
         summarize(totalChangedFiles = X.changed_files.sum())
      );

result5 = pd.merge(result4, groupByResultChangeFilesDf, how='left',  on=['week_start_utc']);
#---------------------------------------------------
# find total number of pull request created in a week by a user
groupByResultPullRequestsCreatedDf = (filteredUserData >>
        group_by(X.week_start_utc)>>
         summarize(totalPullRequestsCreated = X.pull_created_at.count())
      );

result6 = pd.merge(result5, groupByResultPullRequestsCreatedDf, how='left',  on=['week_start_utc']);
#-------------------------------------------------
# find total number of pull request merged in a week by a user
groupByResultPullRequestsMergedDf = (filteredUserData >>
        group_by(X.week_start_utc)>>
         summarize(totalPullRequestsMerged = X.pull_merged_at.count())
      );

result7 = pd.merge(result6, groupByResultPullRequestsMergedDf, how='left',  on=['week_start_utc']);
#-------------------------------------------------
# find total number of pull request review closed in a week by a user
groupByResultPullRequestClosedDf = (filteredUserData >>
        group_by(X.week_start_utc)>>
         summarize(totalPullRequestsClosed = X.pull_closed_at.count())
      );

result8 = pd.merge(result7, groupByResultChangeFilesDf, how='left',  on=['week_start_utc']);
#-------------------------------------------------
# filter information from issue_user_bulk collection for a specific user
collectionDf = issuesUserBulkDf ;
collectionUserFieldName = 'issuer_id' ;
operationFieldName = 'issue_created_at';
filteredIssueData = tableOperation(collectionDf,collectionUserFieldName,userId,operationFieldName,firstDurationStartEp,lastDurationEndEp);
#-----------------------------------------------------------------------------------------------------------------------------------
# find total number of issues created in a week by a user
groupByResultIssuesInWeekDf = (filteredIssueData >>
        group_by(X.week_start_utc)>>
         summarize(totalIssuesInWeek = X.issue_created_at.count())
      );

result9 = pd.merge(result8, groupByResultIssuesInWeekDf, how='left',  on=['week_start_utc']);
#------------------------------------------------------------------------------------------------------------
# find total number of issues created by a user in a week  and that which are assigned 
IssueWithAssignessDf = filteredIssueData[filteredIssueData.assignees_ids.notnull()];
groupByResultAssignedIssuesInWeekDf = (IssueWithAssignessDf >>
        group_by(X.week_start_utc)>>
         summarize(totalAssignedIssuesInWeek = X.issue_closed_at.count())
      );

result10 = pd.merge(result9, groupByResultAssignedIssuesInWeekDf, how='left',  on=['week_start_utc']);
#------------------------------------------------------------------------------------------------------------
# find total number of issues created by a user in a week  and that which are not  assigned
#IssueWithNoAssigneesDf = filteredIssueData[filteredIssueData.assignees_ids.isnull()];
#groupByResultNoAssigneeIssuesInWeekDf = (IssueWithNoAssigneesDf >>
#        group_by(X.week_start_utc)>>
#         summarize(totalNotAssignedIssuesInWeek = X.issue_closed_at.count())
#      );
#
#result11 = pd.merge(result10, groupByResultNoAssigneeIssuesInWeekDf, how='left',  on=['week_start_utc']);
##



