/**
 * Copyright (c) 2014 Red Hat, Inc.
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */
package com.redhat.rhn.frontend.xmlrpc.user.external.test;

import com.redhat.rhn.domain.org.usergroup.UserExtGroup;
import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.domain.user.UserFactory;
import com.redhat.rhn.frontend.xmlrpc.PermissionCheckFailureException;
import com.redhat.rhn.frontend.xmlrpc.test.BaseHandlerTestCase;
import com.redhat.rhn.frontend.xmlrpc.user.external.UserExternalHandler;
import com.redhat.rhn.testing.TestUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class UserExternalHandlerTest extends BaseHandlerTestCase {

    private UserExternalHandler handler = new UserExternalHandler();
    private static final String NAME = "My External Group Name" + TestUtils.randomString();
    private static List<String> roles = Arrays.asList(
            RoleFactory.SYSTEM_GROUP_ADMIN.getLabel(),
            RoleFactory.MONITORING_ADMIN.getLabel());

    public void testExternalGroup() {
        //admin should be able to call list users, regular should not
        UserExtGroup result = handler.createExternalGroup(adminKey, NAME, roles);
        assertNotNull(result);

        //make sure we get a permission exception if a regular user tries to get the user
        //list.
        try {
            result =
                    handler.createExternalGroup(regularKey,
                            "another group" + TestUtils.randomString(), roles);
            fail();
        }
        catch (PermissionCheckFailureException e) {
            //success
        }

        //can't add the same group twice
        try {
            result = handler.createExternalGroup(adminKey, NAME, roles);
            fail();
        }
        catch (PermissionCheckFailureException e) {
            //success
        }

        //make sure at least this group is in the list
        List<UserExtGroup> groups = handler.listExternalGroups(adminKey);
        Set<String> names = new HashSet<String>();
        for (UserExtGroup g : groups) {
            names.add(g.getLabel());
        }
        assertTrue(names.contains(NAME));

        //regular user can't update
        try {
            handler.setExternalGroupRoles(regularKey, NAME, roles);
            fail();
        }
        catch (PermissionCheckFailureException e) {
            //success
        }

        //set org_admin, make sure we get all implied roles. implicitly testing get.
        handler.setExternalGroupRoles(adminKey, NAME,
                Arrays.asList(RoleFactory.ORG_ADMIN.getLabel()));
        UserExtGroup group = handler.getExternalGroup(adminKey, NAME);
        assertTrue(group.getRoles().size() == UserFactory.IMPLIEDROLES.size() + 1);

        //if we set just two roles all others should be deleted
        handler.setExternalGroupRoles(adminKey, NAME, roles);
        group = handler.getExternalGroup(adminKey, NAME);
        assertTrue(group.getRoles().size() == 2);

        //regular user can't delete
        int success = -1;
        try {
            success = handler.deleteExternalGroup(regularKey, NAME);
            fail();
        }
        catch (PermissionCheckFailureException e) {
            //success
        }

        success = handler.deleteExternalGroup(adminKey, NAME);
        assertTrue(success == 1);
    }

    public void testDefaultOrg() {
        int currentDefault = handler.getDefaultOrg(adminKey);
        handler.setDefaultOrg(adminKey, 0);
        assertTrue(0 == handler.getDefaultOrg(adminKey));

        handler.setDefaultOrg(adminKey, 1);
        assertTrue(1 == handler.getDefaultOrg(adminKey));

        handler.setDefaultOrg(adminKey, currentDefault);
    }

    public void testKeepRoles() {
        boolean currentKeepRoles = handler.getKeepTemporaryRoles(adminKey);
        handler.setKeepTemporaryRoles(adminKey, !currentKeepRoles);
        assertTrue(!currentKeepRoles == handler.getKeepTemporaryRoles(adminKey));
        handler.setKeepTemporaryRoles(adminKey, currentKeepRoles);
    }

    public void testUseOrgUnit() {
        boolean currentUseOrgUnit = handler.getUseOrgUnit(adminKey);
        handler.setUseOrgUnit(adminKey, currentUseOrgUnit);
        assertTrue(!currentUseOrgUnit == handler.getUseOrgUnit(adminKey));
        handler.setUseOrgUnit(adminKey, currentUseOrgUnit);
    }
}