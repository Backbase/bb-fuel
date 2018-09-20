package com.backbase.ct.dataloader.tool;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.fail;

import com.backbase.ct.dataloader.dto.entitlement.JobProfile;
import com.backbase.ct.dataloader.dto.entitlement.Permission;
import com.backbase.ct.dataloader.input.InvalidInputException;
import com.backbase.ct.dataloader.util.ParserUtil;
import com.google.common.base.Splitter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Convenience class to generate json file from specs in Excel (converted to csv).
 */
public class JobProfileJsonGenerator {
    private static final Logger LOGGER = LoggerFactory.getLogger(JobProfileJsonGenerator.class);

    private Set<String> availableBusinessFunctions;

    /**
     * Entitlements business functions must match the ones in the jobProfiles.
     */
    @Before
    public void loadEntitlementsDefinition() {
        Set<String> businessFunctions = new HashSet<>();
        Splitter splitter = Splitter.on(',');

        try (Stream<String> lines = Files.lines(Paths.get(
            ClassLoader.getSystemResource("csv/dbs-entitlements-business-functions.csv").toURI()))) {
            lines.forEach(line -> {
                List<String> data = splitter.splitToList(line);
                businessFunctions.add(data.get(2));
            });
            this.availableBusinessFunctions = businessFunctions;
        } catch (IOException | URISyntaxException e) {
            LOGGER.error("Failed reading file", e);
            fail("Cannot read file " + e.getMessage());
        }
    }

    @Test
    public void testSingleProfile() {
        List<JobProfile> jobProfiles = parseJobProfilesFromCsv(MANAGER_PROFILE_CSV);
        assertThat(jobProfiles, hasSize(1));
    }

    @Test
    public void generateJsonFileForBusinessBanking() {
        generateJsonFileFromCsvFile("dbs-mock-data-jobprofile-business", 3);
    }

    @Test
    public void generateJsonFileForRetail() {
        generateJsonFileFromCsvFile("dbs-mock-data-jobprofiles-retail", 2);
    }

    private void generateJsonFileFromCsvFile(String name, int expectedNumberOfProfiles) {
        List<JobProfile> jobProfiles = parseJobProfilesFromFile("csv/" + name + ".csv");
//        validateJobProfiles(jobProfiles);
        assertThat(jobProfiles, hasSize(expectedNumberOfProfiles));
        try {
            FileOutputStream output = new FileOutputStream(new File("target/" + name + ".json"));
            ParserUtil.convertObjectToJson(output, jobProfiles);
        } catch (IOException e) {
            fail("Could not write generated contents " + e.getMessage());
        }
    }

    private List<JobProfile> parseJobProfilesFromFile(String classPathResource)  {
        List<JobProfile> profiles = null;
        try (Stream<String> lines = Files.lines(Paths.get(
            ClassLoader.getSystemResource(classPathResource).toURI()))) {
            // forget about streaming and reconstruct whole contents
            StringBuilder csv = new StringBuilder();
            lines.forEach(line -> {
                csv.append(line).append('\n');
            });
            profiles = parseJobProfilesFromCsv(csv.toString());
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
            fail("Cannot read jobProfiles file " + e.getMessage());
        }
        return profiles;
    }

    private static JobProfile createJobProfileWithEmptyPermissions() {
        JobProfile profile = new JobProfile();
        List<Permission> permissions = new ArrayList<>();
        profile.setPermissions(permissions);
        return profile;
    }

    private void validateJobProfiles(List<JobProfile> jobProfiles) {
        Set<String> invalidBusinessFunctions = new HashSet<>();
        jobProfiles.forEach(jobProfile -> {
            jobProfile.getPermissions().forEach(permission -> {
                if(!availableBusinessFunctions.contains(permission.getBusinessFunction())) {
                    invalidBusinessFunctions.add(permission.getBusinessFunction());
                }
            });
        });
        if (!invalidBusinessFunctions.isEmpty()) {
            throw new InvalidInputException("jobProfiles contain invalid business functions "
                + new ArrayList<>(invalidBusinessFunctions));
        }

    }

    /**
     * Parse csv rows as in the following definition with coordinates (row,column).
     * For each jobProfile the first row should contain jobProfileName (1,1) and privilege names (1,2+).
     * The next rows contain the business function (2+,2) with permissions priviliges (2+,3+).
     * Only the second row contains additional information about approval level name (2,1)
     */
    private List<JobProfile> parseJobProfilesFromCsv(String csv) {
        List<JobProfile> profiles = new ArrayList<>();
        JobProfile profile = createJobProfileWithEmptyPermissions();

        List<String> lines = Splitter.on('\n').splitToList(csv);
        Splitter splitter = Splitter.on(';');
        List<String> privilegeNames = new ArrayList<>();
        // row 1 starts with counter 0, thus actual row and column number as described above differ 1 (subtract)
        int counter = 0;
        for (String line : lines) {
            String originalLine = line;
            if (line.replaceAll(";", "").length() == 0) {
                counter = 0;
                profile = createJobProfileWithEmptyPermissions();
                continue;
            }
            if (counter == 0) {
                line = line.toLowerCase();
                profiles.add(profile);
            }
            List<String> data = splitter.splitToList(line);
            List<String> privilegeChecks = data.subList(2, data.size());
            if (counter == 0) {
                profile.setJobProfileName(splitter.splitToList(originalLine).get(0));
                privilegeNames = privilegeChecks;
            } else {
                if (counter == 1) {
                    String[] labelValue = data.get(0).split(":");
                    if (labelValue.length > 1) {
                        profile.setApprovalLevel(labelValue[1].trim());
                    }
                }
                List<String> privileges = new ArrayList<>();
                Permission permission = Permission.builder()
                    .businessFunction(data.get(1))
                    .privileges(privileges).build();
                int privilegeCounter = 0;
                for (String privilege : privilegeChecks) {
                    if ("y".equalsIgnoreCase(privilege)) {
                        privileges.add(privilegeNames.get(privilegeCounter));
                    }
                    privilegeCounter++;
                }
                if (!privileges.isEmpty()) {
                    profile.getPermissions().add(permission);
                }
            }
            counter++;
        }
        return profiles;
    }

    private static final String MANAGER_PROFILE_CSV = "Manager;;Execute;View;Create;Edit;Delete;Approve;Cancel\n"
        + "(Approval Level - B);US Billpay Enrolment;Y;Y;-;-;-;-;-\n"
        + ";Product Summary;-;Y;-;Y;-;-;-\n"
        + ";Audit;-;Y;Y;-;-;-;-\n"
        + ";US Billpay Payments;-;Y;Y;Y;Y;-;-\n"
        + ";Manage Legal Entities;-;Y;-;-;-;-;-\n"
        + ";Manage Shadow Limits;-;Y;N;N;N;-;-\n"
        + ";Manage Permissions;-;Y;N;N;-;-;-\n"
        + ";Assign pairs of FAG/DAG;-;Y;Y;Y;-;-;-\n"
        + ";US Domestic Wire;-;Y;Y;Y;Y;Y;Y\n"
        + ";Manage Users;-;Y;-;-;-;-;-\n"
        + ";US Billpay Payees;-;Y;Y;Y;Y;-;-\n"
        + ";Manage Product Groups;-;Y;Y;Y;Y;Y;-\n"
        + ";Intra Company Payments;-;Y;Y;Y;Y;Y;Y\n"
        + ";Manage Service Agreements;-;Y;N;N;N;N;-\n"
        + ";US Billpay Payees-Summary;-;Y;-;-;-;-;-\n"
        + ";SEPA CT;-;Y;Y;Y;Y;Y;Y\n"
        + ";US Billpay Accounts;-;Y;-;-;-;-;-\n"
        + ";Transactions;-;Y;-;Y;-;-;-\n"
        + ";Contacts;-;Y;Y;Y;Y;Y;-\n"
        + ";Manage Limits;-;Y;N;N;N;-;-\n"
        + ";Manage Action Recipes;Y;Y;Y;Y;Y;-;-\n"
        + ";Access Actions History;Y;Y;-;-;-;-;-\n"
        + ";Manage Statements;-;Y;-;-;-;-;-\n"
        + ";Manage Notifications;-;Y;Y;-;Y;-;-\n"
        + ";Manage Job Profiles;-;Y;Y;Y;Y;Y;-\n"
        + ";Manage Users in Service Agreement;-;Y;N;N;-;-;-\n"
        + ";US Foreign Wire;-;Y;Y;Y;Y;Y;Y\n"
        + ";US Billpay Payees-Search;Y;Y;-;-;-;-;-";
}
