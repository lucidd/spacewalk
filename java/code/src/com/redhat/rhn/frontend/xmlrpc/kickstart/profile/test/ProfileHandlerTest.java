/**
 * Copyright (c) 2008 Red Hat, Inc.
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
package com.redhat.rhn.frontend.xmlrpc.kickstart.profile.test;

import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.Date;

import com.redhat.rhn.domain.kickstart.KickstartData;
import com.redhat.rhn.domain.kickstart.KickstartFactory;
import com.redhat.rhn.domain.kickstart.KickstartCommandName;
import com.redhat.rhn.domain.kickstart.KickstartCommand;
import com.redhat.rhn.domain.kickstart.test.KickstartDataTest;
import com.redhat.rhn.domain.rhnpackage.Package;
import com.redhat.rhn.domain.rhnpackage.test.PackageTest;
import com.redhat.rhn.domain.token.ActivationKey;
import com.redhat.rhn.frontend.dto.kickstart.KickstartOptionValue;
import com.redhat.rhn.frontend.xmlrpc.kickstart.profile.ProfileHandler;
import com.redhat.rhn.frontend.xmlrpc.test.BaseHandlerTestCase;
import com.redhat.rhn.manager.kickstart.KickstartOptionsCommand;
import com.redhat.rhn.manager.token.ActivationKeyManager;

/**
 * ProfileHandlerTest
 * @version $Rev$
 */
public class ProfileHandlerTest extends BaseHandlerTestCase {
    
    private ProfileHandler handler = new ProfileHandler();
    
    public void testCompareActivationKeys() throws Exception {
        // Setup
        KickstartData ks1 = KickstartDataTest.createKickstartWithProfile(admin);
        KickstartData ks2 = KickstartDataTest.createKickstartWithProfile(admin);
        
        ActivationKeyManager manager = ActivationKeyManager.getInstance();
        ActivationKey activationKey1 = manager.createNewActivationKey(admin, "Test1");
        ActivationKey activationKey2 = manager.createNewActivationKey(admin, "Test2");
        ActivationKey activationKey3 = manager.createNewActivationKey(admin, "Test3");
        
        ks1.getDefaultRegTokens().add(activationKey1.getToken());
        ks1.getDefaultRegTokens().add(activationKey2.getToken());
        
        ks2.getDefaultRegTokens().add(activationKey1.getToken());
        ks2.getDefaultRegTokens().add(activationKey3.getToken());
        
        KickstartFactory.saveKickstartData(ks1);
        KickstartFactory.saveKickstartData(ks2);
        
        // Test
        Map<String, List<ActivationKey>> keysDiff =
            handler.compareActivationKeys(adminKey, ks1.getLabel(), ks2.getLabel());
        
        // Verify
        assertNotNull(keysDiff);

        List<ActivationKey> ks1KeyList = keysDiff.get(ks1.getLabel());
        assertNotNull(ks1KeyList);
        assertEquals(ks1KeyList.size(), 1);
        
        ActivationKey ks1DiffKey = ks1KeyList.iterator().next();
        assertEquals(ks1DiffKey.getToken(), activationKey2.getToken());
        
        List<ActivationKey> ks2KeyList = keysDiff.get(ks2.getLabel());
        assertNotNull(ks2KeyList);
        assertEquals(ks2KeyList.size(), 1);
        
        ActivationKey ks2DiffKey = ks2KeyList.iterator().next();
        assertEquals(ks2DiffKey.getToken(), activationKey3.getToken());
    }
    
    public void testCompareActivationKeysSameProfile() throws Exception {
        // Setup
        KickstartData ks1 = KickstartDataTest.createKickstartWithProfile(admin);
        
        ActivationKeyManager manager = ActivationKeyManager.getInstance();
        ActivationKey activationKey1 = manager.createNewActivationKey(admin, "Test1");
        
        ks1.getDefaultRegTokens().add(activationKey1.getToken());
        
        KickstartFactory.saveKickstartData(ks1);
        
        // Test
        Map<String, List<ActivationKey>> keysDiff =
            handler.compareActivationKeys(adminKey, ks1.getLabel(), ks1.getLabel());
        
        // Verify
        assertNotNull(keysDiff);

        List<ActivationKey> ks1KeyList = keysDiff.get(ks1.getLabel());
        assertNotNull(ks1KeyList);
        assertEquals(ks1KeyList.size(), 0);
    }
    
    public void testCompareActivationKeysNoKeys() throws Exception {
        // Setup
        KickstartData ks1 = KickstartDataTest.createKickstartWithProfile(admin);
        KickstartData ks2 = KickstartDataTest.createKickstartWithProfile(admin);
        
        KickstartFactory.saveKickstartData(ks1);
        KickstartFactory.saveKickstartData(ks2);
        
        // Test
        Map<String, List<ActivationKey>> keysDiff =
            handler.compareActivationKeys(adminKey, ks1.getLabel(), ks2.getLabel());
        
        // Verify
        assertNotNull(keysDiff);

        List<ActivationKey> ks1KeyList = keysDiff.get(ks1.getLabel());
        assertNotNull(ks1KeyList);
        assertEquals(ks1KeyList.size(), 0);
    }
    
    public void testComparePackages() throws Exception {
        // Setup
        
        //   Clear any packages on the profile so we have a known starting state
        KickstartData ks1 = KickstartDataTest.createKickstartWithProfile(admin);
        ks1.getPackageNames().clear();
        
        KickstartData ks2 = KickstartDataTest.createKickstartWithProfile(admin);
        ks2.getPackageNames().clear();
        
        Package package1 = PackageTest.createTestPackage(admin.getOrg());
        Package package2 = PackageTest.createTestPackage(admin.getOrg());
        Package package3 = PackageTest.createTestPackage(admin.getOrg());
        
        ks1.addPackageName(package1.getPackageName());
        ks1.addPackageName(package2.getPackageName());

        ks2.addPackageName(package1.getPackageName());
        ks2.addPackageName(package3.getPackageName());
        
        KickstartFactory.saveKickstartData(ks1);
        KickstartFactory.saveKickstartData(ks2);
        
        // Test
        Map<String, Set<String>> packagesDiff =
            handler.comparePackages(adminKey, ks1.getLabel(), ks2.getLabel());
        
        // Verify
        assertNotNull(packagesDiff);

        Set<String> ks1PackageNameList = packagesDiff.get(ks1.getLabel());
        assertNotNull(ks1PackageNameList);
        assertEquals(1, ks1PackageNameList.size());
        
        String ks1PackageName = ks1PackageNameList.iterator().next();
        assertEquals(package2.getPackageName().getName(), ks1PackageName);
        
        Set<String> ks2PackageNameList = packagesDiff.get(ks2.getLabel());
        assertNotNull(ks2PackageNameList);
        assertEquals(1, ks2PackageNameList.size());
        
        String ks2PackageName = ks2PackageNameList.iterator().next();
        assertEquals(package3.getPackageName().getName(), ks2PackageName);
    }
    
    public void testComparePackagesSameProfile() throws Exception {
        // Setup
        
        //   Clear any packages on the profile so we have a known starting state
        KickstartData ks1 = KickstartDataTest.createKickstartWithProfile(admin);
        ks1.getPackageNames().clear();
                
        Package package1 = PackageTest.createTestPackage(admin.getOrg());
        
        ks1.addPackageName(package1.getPackageName());

        KickstartFactory.saveKickstartData(ks1);
        
        // Test
        Map<String, Set<String>> packagesDiff =
            handler.comparePackages(adminKey, ks1.getLabel(), ks1.getLabel());
        
        // Verify
        assertNotNull(packagesDiff);

        Set<String> ks1PackageNameList = packagesDiff.get(ks1.getLabel());
        assertNotNull(ks1PackageNameList);
        assertEquals(0, ks1PackageNameList.size());
    }
    
    public void testComparePackagesNoPackages() throws Exception {
        // Setup
        
        //   Clear any packages on the profile so we have a known starting state
        KickstartData ks1 = KickstartDataTest.createKickstartWithProfile(admin);
        ks1.getPackageNames().clear();
        
        KickstartData ks2 = KickstartDataTest.createKickstartWithProfile(admin);
        ks2.getPackageNames().clear();
        
        KickstartFactory.saveKickstartData(ks1);
        KickstartFactory.saveKickstartData(ks2);
        
        // Test
        Map<String, Set<String>> packagesDiff =
            handler.comparePackages(adminKey, ks1.getLabel(), ks2.getLabel());
        
        // Verify
        assertNotNull(packagesDiff);

        Set<String> ks1PackageNameList = packagesDiff.get(ks1.getLabel());
        assertNotNull(ks1PackageNameList);
        assertEquals(0, ks1PackageNameList.size());
        
        Set<String> ks2PackageNameList = packagesDiff.get(ks2.getLabel());
        assertNotNull(ks2PackageNameList);
        assertEquals(0, ks2PackageNameList.size());
    }
    
    public void testCompareAdvancedOptions() throws Exception {
        // Setup
        KickstartData ks1 =
            KickstartDataTest.createKickstartWithOptions(admin.getOrg());
        KickstartData ks2 =
            KickstartDataTest.createKickstartWithOptions(admin.getOrg());

        //   Add new value to only one of the profiles so there is something to diff
        KickstartOptionsCommand command1 = new KickstartOptionsCommand(ks1.getId(), admin);
        KickstartCommandName commandName = 
            (KickstartCommandName)command1.getAvailableOptions().iterator().next();
        
        KickstartCommand kc = new KickstartCommand();
        kc.setCommandName(commandName);
        kc.setKickstartData(ks1);
        kc.setCreated(new Date());
        kc.setModified(new Date());                        
        kc.setArguments("test value");                        
        
        command1.getKickstartData().getOptions().add(kc);
        command1.store();
        
        KickstartFactory.saveKickstartData(ks1);
        KickstartFactory.saveKickstartData(ks2);
        
        // Test
        Map<String, List<KickstartOptionValue>> optionsDiff =
            handler.compareAdvancedOptions(adminKey, ks1.getLabel(), ks2.getLabel());
        
        // Verify
        assertNotNull(optionsDiff);

        List<KickstartOptionValue> ks1Values = optionsDiff.get(ks1.getLabel());
        List<KickstartOptionValue> ks2Values = optionsDiff.get(ks2.getLabel());

        assertNotNull(ks1Values);
        assertNotNull(ks2Values);
        
        assertEquals(1, ks1Values.size());
        assertEquals(1, ks2Values.size());

        KickstartOptionValue value1 = ks1Values.get(0);
        assertEquals("test value", value1.getArg());

        KickstartOptionValue value2 = ks2Values.get(0);
        assertEquals("", value2.getArg());
        
        assertEquals(value1.getName(), value2.getName());
    }
}
