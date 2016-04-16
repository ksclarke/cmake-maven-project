package com.googlecode.cmakemavenproject;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

import java.io.File;
import java.io.IOException;
import java.util.Properties;
import org.apache.maven.it.VerificationException;
import org.apache.maven.it.Verifier;
import org.apache.maven.it.util.ResourceExtractor;

/**
 * An abstract test class that handles <code>Verifier</code> configuration.
 * <p/>
 *
 * @author Kevin S. Clarke <ksclarke@gmail.com>
 */
public abstract class CMakeMojoIntegrationTest
{

    // Maven settings.xml file to be used for the test projects
    private static final String SETTINGS = "/settings.xml";

    // CMake-Maven-Plugin version (so we don't have to manually keep in sync)
    private static final String CMP_VERSION = "cmake.project.version";

    // Get the classifier configured by our build process
    private static final String CMAKE_CLASSIFIER = "cmake.classifier";
    
    private static final String CMAKE_NATIVE = "use.native.cmake";

    /**
     * Returns a <code>Verifier</code> that has been configured to use the test
     * repository along with the test project that was passed in as a variable.
     * <p/>
     *
     * @param testName The CMake Maven project to test
     * @return A configured <code>Verifier</code>
     * @throws IOException If there is a problem with configuration.
     * @throws VerificationException If there is a problem with verification.
     */
    protected Verifier getVerifier(String testName) throws IOException,
        VerificationException
    {
        Class<? extends CMakeMojoIntegrationTest> cls = getClass();
        String name = testName.startsWith("/") ? testName : "/" + testName;
        File config = ResourceExtractor.simpleExtractResources(cls, SETTINGS);
        File test = ResourceExtractor.simpleExtractResources(cls, name);
        String settings = config.getAbsolutePath();

        // Construct a verifier that will run our integration tests
        Verifier verifier = new Verifier(test.getAbsolutePath(), settings, true);
        Properties verProperties = verifier.getVerifierProperties();
        Properties sysProperties = verifier.getSystemProperties();

        // We need to pass along the version number of our parent project
        sysProperties.setProperty(CMP_VERSION, System.getProperty(CMP_VERSION));
        
        if (System.getProperty(CMAKE_NATIVE) != null 
                && System.getProperty(CMAKE_NATIVE).equals("true")){
            sysProperties.setProperty(CMAKE_NATIVE, "true");
        }
        // Set the profile that's being used in the running of the tests
        verifier.addCliOption(getActivatedProfile());

        // use.mavenRepoLocal instructs forked tests to use the local repo
        verProperties.setProperty("use.mavenRepoLocal", "true");

        verifier.setAutoclean(true); // Set so clean is run before each test

        return verifier;
    }

    /**
     * Gets the profile that's been trigger via the testing process.
     */
    private String getActivatedProfile() throws VerificationException {
        String classifier = System.getProperty(CMAKE_CLASSIFIER);

        if (classifier.equals("linux64")) {
            return "-Plinux64,-linux32,-windows,-mac64";
        } else if (classifier.equals("linux32")) {
            return "-Plinux32,-linux64,-windows,-mac64";
        } else if (classifier.equals("windows")) {
            return "-Pwindows,-linux32,-linux64,-mac64";
        } else if (classifier.equals("mac64")) {
            return "-Pmac64,-windows,-linux32,-linux64";
        } else {
            throw new VerificationException("Unexpected test profile: " + classifier);
        }
    }
}
