/**
 *    Copyright 2012-2013 Trento RISE
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package eu.trentorise.smartcampus.common;

import java.util.List;

public class ShareVisibility {

	private List<Long> userIds;
	private List<Long> groupIds;
	private List<Long> communityIds;

	private boolean allKnownUsers;
	private boolean allKnownCommunities;
	private boolean allUsers;
	private boolean allCommunities;

	public List<Long> getUserIds() {
		return userIds;
	}

	public void setUserIds(List<Long> userIds) {
		this.userIds = userIds;
	}

	public List<Long> getGroupIds() {
		return groupIds;
	}

	public void setGroupIds(List<Long> groupIds) {
		this.groupIds = groupIds;
	}

	public List<Long> getCommunityIds() {
		return communityIds;
	}

	public void setCommunityIds(List<Long> communityIds) {
		this.communityIds = communityIds;
	}

	public boolean isAllKnownUsers() {
		return allKnownUsers;
	}

	public void setAllKnownUsers(boolean allKnownUsers) {
		this.allKnownUsers = allKnownUsers;
	}

	public boolean isAllCommunities() {
		return allCommunities;
	}

	public boolean isAllKnownCommunities() {
		return allKnownCommunities;
	}

	public void setAllKnownCommunities(boolean allKnownCommunities) {
		this.allKnownCommunities = allKnownCommunities;
	}

	public boolean isAllUsers() {
		return allUsers;
	}

	public void setAllUsers(boolean allUsers) {
		this.allUsers = allUsers;
	}

	public void setAllCommunities(boolean allCommunities) {
		this.allCommunities = allCommunities;
	}

}
