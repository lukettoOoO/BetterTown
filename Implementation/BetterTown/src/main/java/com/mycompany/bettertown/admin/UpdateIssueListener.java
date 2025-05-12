package com.mycompany.bettertown.admin;

import com.mycompany.bettertown.user.*;
import com.mycompany.bettertown.IssueData;

public interface UpdateIssueListener {
    void onIssueAdded(IssueData issueData);
}
