package io.entgra.device.mgt.core.device.mgt.common.authorization;

import java.util.List;

public interface GroupAccessAuthorizationService {

    public boolean isUserAuthorized(int groupId, String username, String[] groupPermissions)
            throws GroupAccessAuthorizationException;

    public boolean isUserAuthorized(int groupId, String[] groupPermissions)
            throws GroupAccessAuthorizationException;

    public GroupAuthorizationResult isUserAuthorized(List<Integer> groupIds, String username, String[] groupPermission)
            throws GroupAccessAuthorizationException;

}
