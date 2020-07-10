import React from 'react';
import { TechPills } from './TechPills';
import 'react-circular-progressbar/dist/styles.css';
import moment from 'moment';
import { IconContext } from "react-icons";

import { AiFillHome, AiOutlineSearch, AiOutlineCrown, AiOutlineLogout
, AiOutlineFunction } from 'react-icons/ai';
import { IoMdRefresh } from 'react-icons/io';

function add(x,y){
	return x+y
}


var subtract = function(x,y){
	return x-y
};

(function () {
var userName = "Steve";

function display(name)
{
alert("MyScript2.js: " + name);
}

display(userName);
})();

class LeftPanelView extends React.Component {
    constructor(props) {
        super(props);

        this.userIDInput = React.createRef();

        this.state = {
            showUserIDInput: false
        };
    };

    showMenuText = (e, show) => {
        if (show) {
            this.setState({
                activeMenu: e.currentTarget.getAttribute('name')
            });
        } else {
            this.setState({
                activeMenu: ''
            });
        }
    }

    showUserIDInput = () => {
        if (this.props.githubUserID === 18552651 || this.props.githubUserID === 4823303 || this.props.githubUserID === 65643749) {
            this.setState({
                showUserIDInput: !this.state.showUserIDInput
            });
        }
    }

    onLogoutClick = () => {
        localStorage.clear();
        window.history.go(0);
    }

    changeUserID = () => {
        this.props.changeUserID(this.userIDInput.current.value, this.props.userName);
    }

    render() {
        let {userID, following, followers, company, location, name} = this.props;
        const techArray = [];//["Mongo", "React", "Java", "Javascript", "Python", "SQL", "CSS3", "HTML5"];

        // name = 'John Smith';
        // location = 'Palo Alto, CA';
        // followers = 178;
        // following = 65;
        // company = 'Twitter';

        const value = { color: '#d8d0d0', size: '25px' };
        const selectedValue = { color: '#2196f3', size: '25px' };

        return (
            <React.Fragment>
                <div className='navi_bar'>
                    <img onClick={this.showUserIDInput} src='images/GitAnalytics_logo_002.png' alt='logo' className='logo'></img>
                    {this.state.showUserIDInput && (
                        <div className="userIDInputDiv">
                            <input ref={this.userIDInput} type="text" />
                            <button onClick={this.changeUserID}>Submit</button>
                        </div>
                    )}

                    <div onClick={() => {this.props.changeCurrentPage('gamification')}} className={this.props.currentPage === 'gamification' ? 'menu selected' : 'menu'} name='Home'>

                        <IconContext.Provider value={this.props.currentPage === 'gamification' ? selectedValue : value}>
                            <AiFillHome  />
                        </IconContext.Provider>

                    </div>

                    <div onClick={() => {this.props.changeCurrentPage('codeSearch')}} className={this.props.currentPage === 'codeSearch' ? 'menu selected codeSearchIcon' : 'menu codeSearchIcon'} name='Code Search'>
                        <IconContext.Provider value={this.props.currentPage === 'codeSearch' ? selectedValue : value}>
                            <AiOutlineSearch  />
                        </IconContext.Provider>
                    </div>

                    <div onClick={() => {this.props.changeCurrentPage('functionShare')}} className={this.props.currentPage === 'functionShare' ? 'menu selected functionShareIcon' : 'menu functionShareIcon'} name='Function Share'>
                        <IconContext.Provider value={this.props.currentPage === 'functionShare' ? selectedValue : value}>
                            <AiOutlineFunction  />
                        </IconContext.Provider>
                    </div>

                    <div onClick={() => {this.props.changeCurrentPage('leaderBoard')}} className={this.props.currentPage === 'leaderBoard' ? 'menu selected' : 'menu'} name='Leaderboard'>
                        <IconContext.Provider value={this.props.currentPage === 'leaderBoard' ? selectedValue : value}>
                            <AiOutlineCrown  />
                        </IconContext.Provider>
                    </div>

                    <div onClick={() => {this.props.refreshData(this.props.userName)}} className='menu refresh' name='Refresh'>
                        <IconContext.Provider value={selectedValue}>
                            <IoMdRefresh  />
                        </IconContext.Provider>
                    </div>

                    <div onClick={this.onLogoutClick} className='menu logout' name='Logout'>
                        <IconContext.Provider value={selectedValue}>
                            <AiOutlineLogout  />
                        </IconContext.Provider>
                    </div>
                </div>
                <div className="left_panel">

                    <div className='left_panel_cnt'>
                        <div className='profile_cnt'>
                            <img src={ `https://avatars0.githubusercontent.com/u/${userID}?v=4` } alt='profile' className='profile_img'></img>
                        </div>

                        <div className='user_info'>
                            <p className='name bold'>{ name || '' }</p>

                            <p className="lastSeenInfo">
                                <span>Last seen on: </span>
                                <span className='bold'>{moment(this.props.last_activity).format('DD MMM YYYY')}</span>
                            </p>

                            {/*<p className="shareProfile">Share public profile</p>*/}
                        </div>
                    </div>
                    <div className='left_panel_cnt'>
                        <div className='otherInfo'>
                            <table>
                                <tbody>
                                      <tr>
                                        <td>Company: </td>
                                        <td className='bold'>{company || ''}</td>
                                      </tr>
                                      <tr>
                                        <td>Location: </td>
                                        <td className='bold'>{location || ''}</td>
                                      </tr>
                                      <tr>
                                        <td>Followers: </td>
                                        <td className='bold'>{followers || 0}</td>
                                      </tr>
                                      <tr>
                                        <td>Following: </td>
                                        <td className='bold'>{following || 0}</td>
                                      </tr>
                                </tbody>
                            </table>
                            <div>

                            </div>
                        </div>
                        <div className='skills'>
                            {
                                techArray.map((tech, index) => {
                                    return <TechPills key={index} tech={tech} />
                                })
                            }
                        </div>
                    </div>
                </div>
            </React.Fragment>
        )
    };

    componentDidMount () {

    };
}

export default LeftPanelView;

export const getGamificationObject = (weeklySummary, monthlySummary) => {
    const gamificationObject = {};

    analyzeMonthlySummary(monthlySummary, weeklySummary, gamificationObject);

    analyzeWeeklySummary(weeklySummary, gamificationObject);

    return gamificationObject;
}

const analyzeMonthlySummary = (monthlySummary, weeklySummary, gamificationObject) => {
    const currentMonthStartDate = moment().startOf('month');

    let runningDate, currentMonthSummary = {}, currentMonthSummaryObjFound = false;

    let totalCommits = 0, totalLOCAdded_Major = 0, totalLOCAdded_Minor = 0, totalLOCDeleted_Major = 0, totalLOCDeleted_Minor = 0;
    let totalPR = 0, totalPR_accepted = 0, totalPR_rejected = 0, totalIssuesFiled = 0, totalIssuesAccepted = 0, totalIssuesRejected = 0;
    let totalFilesChanged = 0;

    const monthlyCommitsTrendBarChartDataArray = [], monthlyTrendBarChartDataTicksArray = [];
    const monthlyPullRequestsTrendBarChartDataArray = [], monthlyIssuesTrendBarChartDataArray = [];

    monthlyCommitsTrendBarChartDataArray.push(['MonthStartDate', 'Commits']);
    monthlyPullRequestsTrendBarChartDataArray.push(['MonthStartDate', 'Pull Requests']);
    monthlyIssuesTrendBarChartDataArray.push(['MonthStartDate', 'Issues']);

    monthlySummary.forEach((monthlySummaryObj) => {
        runningDate = moment(monthlySummaryObj['month_start_date']);

        if (!currentMonthSummaryObjFound) {
            if (currentMonthStartDate._d.getDate() === runningDate._d.getDate() && currentMonthStartDate._d.getMonth() === runningDate._d.getMonth() && currentMonthStartDate._d.getYear() === runningDate._d.getYear()) {
                currentMonthSummary = monthlySummaryObj;
                currentMonthSummaryObjFound = true;
            }
        }

        totalCommits = totalCommits + Number(monthlySummaryObj.commits_frm_repos);
        totalLOCAdded_Major = totalLOCAdded_Major + Number(monthlySummaryObj.LOC_added_major);
        totalLOCAdded_Minor = totalLOCAdded_Minor + Number(monthlySummaryObj.LOC_added_minor);
        totalLOCDeleted_Major = totalLOCDeleted_Major + Number(monthlySummaryObj.LOC_deleted_major);
        totalLOCDeleted_Minor = totalLOCDeleted_Minor + Number(monthlySummaryObj.LOC_deleted_minor);
        totalPR = totalPR + Number(monthlySummaryObj.total_PR);
        totalPR_accepted = totalPR_accepted + Number(monthlySummaryObj.PR_accepted);
        totalPR_rejected = totalPR_rejected + Number(monthlySummaryObj.PR_rejected);
        totalIssuesFiled = totalIssuesFiled + Number(monthlySummaryObj.issues_filed);
        totalIssuesAccepted = totalIssuesAccepted + Number(monthlySummaryObj.issues_accepted);
        totalIssuesRejected = totalIssuesRejected + Number(monthlySummaryObj.issues_rejected);
        totalFilesChanged = totalFilesChanged + Number(monthlySummaryObj.files_changed_commits)

        // monthlyCommitsTrendBarChartDataArray.push([runningDate._locale._monthsShort[runningDate.month()] + '-' + runningDate.year(), Math.floor(Math.random() * 20) + 1]);//
        monthlyCommitsTrendBarChartDataArray.push([new Date (monthlySummaryObj['month_start_date']), monthlySummaryObj['commits_frm_repos']]);
        monthlyPullRequestsTrendBarChartDataArray.push([new Date (monthlySummaryObj['month_start_date']), monthlySummaryObj['total_PR']]);
        monthlyIssuesTrendBarChartDataArray.push([new Date (monthlySummaryObj['month_start_date']), monthlySummaryObj['total_PR']]);

        monthlyTrendBarChartDataTicksArray.push(new Date (monthlySummaryObj['month_start_date']));
    });

    const totalWeeks = weeklySummary.length;
    const totalMonths = monthlySummary.length;

    gamificationObject['currentMonthSummary'] = currentMonthSummary;

    gamificationObject['avgWeeklyCommits'] = totalCommits / totalWeeks;
    gamificationObject['avgMonthlyCommits'] = totalCommits / totalMonths;

    gamificationObject['avgWeeklyLOCAdded_Major'] = totalLOCAdded_Major / totalWeeks;
    gamificationObject['avgMonthlyLOCAdded_Major'] = totalLOCAdded_Major / totalMonths;

    gamificationObject['avgWeeklyLOCAdded_Minor'] = totalLOCAdded_Minor / totalWeeks;
    gamificationObject['avgMonthlyLOCAdded_Minor'] = totalLOCAdded_Minor / totalMonths;

    gamificationObject['avgWeeklyLOCDeleted_Major'] = totalLOCDeleted_Major / totalWeeks;
    gamificationObject['avgMonthlyLOCDeleted_Major'] = totalLOCDeleted_Major / totalMonths;

    gamificationObject['avgWeeklyLOCDeleted_Minor'] = totalLOCDeleted_Minor / totalWeeks;
    gamificationObject['avgMonthlyLOCDeleted_Minor'] = totalLOCDeleted_Minor / totalMonths;

    gamificationObject['avgWeeklyPR'] = totalPR / totalWeeks;
    gamificationObject['avgMonthlyPR'] = totalPR / totalMonths;

    gamificationObject['avgWeeklyPR_accepted'] = totalPR_accepted / totalWeeks;
    gamificationObject['avgMonthlyPR_accepted'] = totalPR_accepted / totalMonths;

    gamificationObject['avgWeeklyPR_rejected'] = totalPR_rejected / totalWeeks;
    gamificationObject['avgMonthlyPR_rejected'] = totalPR_rejected / totalMonths;

    gamificationObject['avgWeeklyIssuesFiled'] = totalIssuesFiled / totalWeeks;
    gamificationObject['avgMonthlyIssuesFiled'] = totalIssuesFiled / totalMonths;

    gamificationObject['avgWeeklyIssuesAccepted'] = totalIssuesAccepted / totalWeeks;
    gamificationObject['avgMonthlyIssuesAccepted'] = totalIssuesAccepted / totalMonths;

    gamificationObject['avgWeeklyFilesChanged'] = totalFilesChanged / totalWeeks;
    gamificationObject['avgMonthlyFilesChanged'] = totalFilesChanged / totalMonths;

    gamificationObject['totalCommits'] = totalCommits;
    gamificationObject['totalLOCAdded_Major'] = totalLOCAdded_Major;
    gamificationObject['totalLOCAdded_Minor'] = totalLOCAdded_Minor;
    gamificationObject['totalLOCDeleted_Major'] = totalLOCDeleted_Major;
    gamificationObject['totalLOCDeleted_Minor'] = totalLOCDeleted_Minor;
    gamificationObject['totalPR'] = totalPR;
    gamificationObject['totalPR_accepted'] = totalPR_accepted;
    gamificationObject['totalPR_rejected'] = totalPR_rejected;
    gamificationObject['totalIssuesFiled'] = totalIssuesFiled;
    gamificationObject['totalIssuesAccepted'] = totalIssuesAccepted;
    gamificationObject['totalIssuesRejected'] = totalIssuesRejected;
    gamificationObject['totalFilesChanged'] = totalFilesChanged;
    gamificationObject['leaderBoardObject'] = {};

    gamificationObject['monthlyCommitsTrendBarChartDataArray'] = monthlyCommitsTrendBarChartDataArray;
    gamificationObject['monthlyPullRequestsTrendBarChartDataArray'] = monthlyPullRequestsTrendBarChartDataArray;
    gamificationObject['monthlyIssuesTrendBarChartDataArray'] = monthlyIssuesTrendBarChartDataArray;

    gamificationObject['monthlyTrendBarChartDataTicksArray'] = monthlyTrendBarChartDataTicksArray;

    gamificationObject['monthlySummary'] = monthlySummary;
}

const analyzeWeeklySummary = (weeklySummary, gamificationObject) => {
    const currentWeekMonday = moment().startOf('isoweek');

    let runningDate, currentWeekSummary = {}, currentWeekSummaryObjFound = false;

    const weeklyCommitsTrendBarChartDataArray = [], weeklyTrendBarChartDataTicksArray = [];
    weeklyCommitsTrendBarChartDataArray.push(['WeekStartDate', 'Commits']);

    const weeklyTotalContributionsArr = [];
    let weeklyTotalContributions = 0;

    const oneYearBack = (moment().subtract(1, 'years'));
    const oneYearBackTime = oneYearBack._d.getTime();

    const nextMonthStartDate = moment(oneYearBackTime).add(1, 'months').startOf('month');
    const nextMonthStartDateTime = nextMonthStartDate._d.getTime()


    weeklySummary.forEach((weeklySummaryObj) => {
        runningDate = moment(weeklySummaryObj['week_start_date']);

        if (!currentWeekSummaryObjFound) {
            if (currentWeekMonday._d.getDate() === runningDate._d.getDate() && currentWeekMonday._d.getMonth() === runningDate._d.getMonth() && currentWeekMonday._d.getYear() === runningDate._d.getYear()) {
                currentWeekSummary = weeklySummaryObj;
                currentWeekSummaryObjFound = true;
            }
        }

        weeklyTotalContributions = weeklySummaryObj.commits_frm_repos + weeklySummaryObj.total_PR + weeklySummaryObj.issues_filed;

        if (isNaN(weeklyTotalContributions)) {
            weeklyTotalContributions = 0;
        }

        if (weeklySummaryObj['week_start_date'] >= nextMonthStartDateTime) {
            weeklyTotalContributionsArr.push({
                weekStartDate: new Date (weeklySummaryObj['week_start_date']),
                weeklyTotalContributions
            });
        }

        weeklyCommitsTrendBarChartDataArray.push([new Date (weeklySummaryObj['week_start_date']), weeklySummaryObj['commits_frm_repos']]);
        weeklyTrendBarChartDataTicksArray.push(new Date (weeklySummaryObj['week_start_date']));
    });

    gamificationObject['weeklyCommitsTrendBarChartDataArray'] = weeklyCommitsTrendBarChartDataArray;
    gamificationObject['weeklyTotalContributionsArr'] = weeklyTotalContributionsArr;
    gamificationObject['currentWeekSummary'] = currentWeekSummary;
    gamificationObject['weeklySummary'] = weeklySummary;
}

export const getPercentDifference = (firstNumber, secondNumber) => {
    firstNumber = Number(firstNumber);
    secondNumber = Number(secondNumber);

    if (secondNumber === 0) {
        return {
            difference: 0,
            sign: 'positive'
        }
    }

    const percent = (firstNumber * 100) / secondNumber;

    let difference = 0, sign = '';
    if (percent > 100) {
        difference = percent - 100;
        sign = 'positive';
    } else {
        difference = 100 - percent;
        sign = 'negative';
    }

    return {
        difference,
        sign
    }
}

export const getRepositoriesObject = (reposArr) => {

    let totalRepos = reposArr.length, currentRepo;
    let ownedReposIDs = [], forkedReposIDs = [];

    let totalStars = 0, totalWatch = 0, totalForks = 0, totalSubscribers = 0;

    for (var i = 0; i < totalRepos; i++) {
        currentRepo = reposArr[i];

        if (currentRepo['is_fork'] === 'True') {
            forkedReposIDs.push(currentRepo.repo_id);
        } else {
            ownedReposIDs.push(currentRepo.repo_id);
        }

        totalStars = totalStars + Number(currentRepo['stars']);
        totalWatch = totalWatch + Number(currentRepo['watch']);
        totalForks = totalForks + Number(currentRepo['forks_count']);
        totalSubscribers = totalSubscribers + Number(currentRepo['subscribers_count']);
    }

    return {
        totalRepos,
        forkedRepos: forkedReposIDs.length,
        ownedRepos: ownedReposIDs.length,
        ownedReposIDs,
        forkedReposIDs,
        totalStars,
        totalWatch,
        totalForks,
        totalSubscribers
    }
}

export const getCommitsObject = (commits, forkedReposIDs, ownedReposIDs) => {
    let commitsInOwnedRepos = 0, commitsInForkedRepos = 0, currentCommit;

    for (var i = 0; i < commits.length; i++) {
        currentCommit = commits[i];

        if (ownedReposIDs.indexOf(currentCommit.repo_id) > -1) {
            commitsInOwnedRepos = commitsInOwnedRepos + 1;
        } else if (forkedReposIDs.indexOf(currentCommit.repo_id) > -1) {
            commitsInForkedRepos = commitsInForkedRepos + 1;
        }
    }

    return {
        commitsInOwnedRepos,
        commitsInForkedRepos
    }
}

export const getPullsObject = (pulls, forkedReposIDs, ownedReposIDs, userID) => {
    let pullsByUser = 0, pullsByOthers = 0, currentPull;

    for (var i = 0; i < pulls.length; i++) {
        currentPull = pulls[i];

        if (Number(currentPull.pull_requester_id) === userID) {
            pullsByUser = pullsByUser + 1;
        } else {
            pullsByOthers = pullsByOthers + 1;
        }
    }

    return {
        pullsByUser,
        pullsByOthers
    }
}

export const getIssuesObject = (issues, forkedReposIDs, ownedReposIDs, userID) => {
    let issuesByUser = 0, issuesByOthers = 0, currentIssue;

    for (var i = 0; i < issues.length; i++) {
        currentIssue = issues[i];

        if (Number(currentIssue.issuer_id) === userID) {
            issuesByUser = issuesByUser + 1;
        } else {
            issuesByOthers = issuesByOthers + 1;
        }
    }

    return {
        issuesByOthers,
        issuesByUser
    }
}

export const getAllCommitsObject = (allCommits, forkedReposIDs, ownedReposIDs, user_name, name) => {
    let commitsByUser = 0, commitsByOthers = 0, currentCommit;
    let contributors = new Set();

    for (var i = 0; i < allCommits.length; i++) {
        currentCommit = allCommits[i];
        contributors.add(currentCommit.commiter);

        if (currentCommit.commiter === user_name || currentCommit.commiter === name) {
            commitsByUser = commitsByUser + 1;
        } else {
            commitsByOthers = commitsByOthers + 1;
        }
    }

    return {
        commitsByOthers,
        commitsByUser,
        contributors: contributors.length || 0
    }
}

export const getRepoDetailArray = (repositories, pulls, issues, allCommits, userID, userName, fullName, funcClass, fileDetails) => {
    const repoDetailArray = [];

    let currentRepoID, currentRepoName, typeOfRepo, openIssues;
    let pullsByYou = 0, pullsByOthers = 0;
    let issuesByYou = 0, issuesByOthers = 0;
    let commitsByYou = 0, commitsByOthers = 0;
    let allFileDetails = [], allFuncClassDetails = [], funcClassDetailsMetrics, fileDetailsMetrics;

    const pullsByYouFilterFn = (currentRepoID, userID) => {
        return (pull) => {
            return pull['base_repo_id'] === currentRepoID && pull['pull_requester_id'] === userID
        }
    }

    const pullsByOthersFilterFn = (currentRepoID, userID) => {
        return (pull) => {
            return pull['base_repo_id'] === currentRepoID && pull['pull_requester_id'] !== userID
        }
    }

    const issuesByYouFilterFn = (currentRepoID, userID) => {
        return (issue) => {
            return issue['repo_id'] === currentRepoID && issue['issuer_id'] === userID
        }
    }

    const issuesByOthersFilterFn = (currentRepoID, userID) => {
        return (issue) => {
            return issue['repo_id'] === currentRepoID && issue['issuer_id'] !== userID
        }
    }

    const commitsByYouFilterFn = (currentRepoID, userName, fullName) => {
        return (commit) => {
            return commit['repo_id'] === currentRepoID && (commit['commiter'] === "userName" || commit['commiter'] === fullName)
        }
    }

    const commitsByOthersFilterFn = (currentRepoID, userName, fullName) => {
        return (commit) => {
            return commit['repo_id'] === currentRepoID && commit['commiter'] !== userName && commit['commiter'] !== fullName
        }
    }

    const fileDetailsFilterFn = (currentRepoID) => {
        return (fileDetail) => {
            return fileDetail['repo_id'] === currentRepoID
        }
    }

    const funcClassFilterFn = (currentRepoID) => {
        return (funcClassDetail) => {
            return funcClassDetail['repo_id'] === currentRepoID
        }
    }

    for (var i = 0; i < repositories.length; i++) {
        currentRepoID = repositories[i]['repo_id'];
        currentRepoName = repositories[i]['repo_name'];
        typeOfRepo = repositories[i]['is_fork'] === "True" ? "Forked": "Owned";
        openIssues = repositories[i]['open_issues'];

        pullsByYou = 0; pullsByOthers = 0;
        issuesByYou = 0; issuesByOthers = 0;
        commitsByYou = 0; commitsByOthers = 0;

        pullsByYou = pulls.filter(pullsByYouFilterFn(currentRepoID, userID));

        pullsByYou = pullsByYou.length;

        pullsByOthers = pulls.filter(pullsByOthersFilterFn(currentRepoID, userID));

        pullsByOthers = pullsByOthers.length;

        issuesByYou = issues.filter(issuesByYouFilterFn(currentRepoID, userID));

        issuesByYou = issuesByYou.length;

        issuesByOthers = issues.filter(issuesByOthersFilterFn(currentRepoID, userID));

        issuesByOthers = issuesByOthers.length;

        commitsByYou = allCommits.filter(commitsByYouFilterFn(currentRepoID, userName, fullName));

        commitsByYou = commitsByYou.length;

        commitsByOthers = allCommits.filter(commitsByOthersFilterFn(currentRepoID, userName, fullName));

        commitsByOthers = commitsByOthers.length;

        allFileDetails = fileDetails.filter(fileDetailsFilterFn(currentRepoID));

        fileDetailsMetrics = getFileDetailsMetrics(allFileDetails);

        allFuncClassDetails = funcClass.filter(funcClassFilterFn(currentRepoID)); //264420849

        funcClassDetailsMetrics = getFuncClassDetailsMetrics(allFuncClassDetails);

        repoDetailArray.push({
            repoID: currentRepoID,
            repoName: currentRepoName,
            typeOfRepo,
            openIssues,
            pullsByYou,
            pullsByOthers,
            issuesByYou,
            issuesByOthers,
            commitsByYou,
            commitsByOthers,
            allFileDetails,
            allFuncClassDetails,
            funcClassDetailsMetrics,
            fileDetailsMetrics
        });
    }

    return repoDetailArray;
}

const getFuncClassDetailsMetrics = (allFuncClassDetails) => {
    let currentFuncClass;
    let smallFunctions = 0, functionWithDocString = 0;

    for (var i = 0; i < allFuncClassDetails.length; i++) {
        currentFuncClass = allFuncClassDetails[i];

        if (currentFuncClass.func_size < 10) {
            smallFunctions = smallFunctions + 1;
        }

        if (typeof currentFuncClass.docString !== 'undefined') {
            functionWithDocString = functionWithDocString + 1;
        }
    }

    return {
        totalFunctions: allFuncClassDetails.length,
        smallFunctions,
        functionWithDocString
    }
}

const getFileDetailsMetrics = (allFileDetails) => {
    let currentFileDetail;
    let totalFunctions = 0, totalClasses = 0, totalLinesOfComment = 0, totalLinesOfCode = 0;
    const totalFiles = allFileDetails.length;

    for (var i = 0; i < totalFiles; i++) {
        currentFileDetail = allFileDetails[i];

        totalFunctions = totalFunctions + Number(currentFileDetail.total_functions);
        totalClasses = totalClasses + Number(currentFileDetail.total_classes);
        totalLinesOfComment = totalLinesOfComment + Number(currentFileDetail.lines_of_comments);
        totalLinesOfCode = totalLinesOfCode + Number(currentFileDetail.lines_of_code);
    }

    return {
        totalFunctions,
        totalClasses,
        totalLinesOfComment,
        totalLinesOfCode,
        totalFiles
    }
}

export const getLeaderboardObject = (leaderBoard, userID) => {

    let totalCommitsRank, totalPRRank, totalIssuesRank;

    const totalUsers = leaderBoard.length;

    let sortedOnCommits = sortArrayOfObjects(leaderBoard, 'total_commits');
    for (let i = 0; i < totalUsers; i++) {

        if (sortedOnCommits[i]['user_id'] === userID) {
            totalCommitsRank = i + 1;
            break;
        }
    }


    let sortedOnPR = sortArrayOfObjects(leaderBoard, 'total_PR');
    for (let i = 0; i < totalUsers; i++) {

        if (sortedOnPR[i]['user_id'] === userID) {
            totalPRRank = i + 1;
            break;
        }
    }

    let sortedOnIssues = sortArrayOfObjects(leaderBoard, 'total_issues');
    for (let i = 0; i < totalUsers; i++) {

        if (sortedOnIssues[i]['user_id'] === userID) {
            totalIssuesRank = i + 1;
            break;
        }
    }

    return {
        totalCommitsRank, totalPRRank, totalIssuesRank, totalUsers
    }
}

const sortArrayOfObjects = (array, propertyName) => {
    function compare( a, b ) {
      if ( a[propertyName] > b[propertyName] ){
        return -1;
      }
      if ( a[propertyName] < b[propertyName] ){
        return 1;
      }
      return 0;
    }

    let sortedArray = array.sort(compare);

    return sortedArray;
}
