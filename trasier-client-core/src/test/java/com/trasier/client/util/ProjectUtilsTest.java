package com.trasier.client.util;

import org.junit.Test;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

public class ProjectUtilsTest {

    @Test
    public void testPropertyUnset() {
        String projectVersion = ProjectUtils.getProjectVersion();

        assertNotNull(projectVersion);
        assertNotEquals("unknown", projectVersion);
    }

}