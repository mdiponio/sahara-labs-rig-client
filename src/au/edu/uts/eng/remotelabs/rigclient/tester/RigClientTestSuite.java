/**
 * SAHARA Rig Client
 * 
 * Software abstraction of physical rig to provide rig session control
 * and rig device control. Automatically tests rig hardware and reports
 * the rig status to ensure rig goodness.
 *
 * @license See LICENSE in the top level directory for complete license terms.
 *
 * Copyright (c) 2009, University of Technology, Sydney
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright notice, 
 *    this list of conditions and the following disclaimer.
 *  * Redistributions in binary form must reproduce the above copyright 
 *    notice, this list of conditions and the following disclaimer in the 
 *    documentation and/or other materials provided with the distribution.
 *  * Neither the name of the University of Technology, Sydney nor the names 
 *    of its contributors may be used to endorse or promote products derived from 
 *    this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE 
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE 
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL 
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR 
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER 
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, 
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE 
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * @author Michael Diponio (mdiponio)
 * @date 16th October 2009
 *
 * Changelog:
 * - 16/10/2009 - mdiponio - Initial file creation.
 */
package au.edu.uts.eng.remotelabs.rigclient.tester;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import au.edu.uts.eng.remotelabs.rigclient.rig.control.tests.AbstractBatchRunnerTester;
import au.edu.uts.eng.remotelabs.rigclient.rig.control.tests.ConfiguredBatchRunnerTester;
import au.edu.uts.eng.remotelabs.rigclient.rig.internal.tests.DirectoryCopierTester;
import au.edu.uts.eng.remotelabs.rigclient.rig.internal.tests.DirectoryZipperTester;
import au.edu.uts.eng.remotelabs.rigclient.rig.internal.tests.MacroSubstituerTester;
import au.edu.uts.eng.remotelabs.rigclient.rig.primitive.tests.PrimitiveCacheTester;
import au.edu.uts.eng.remotelabs.rigclient.rig.primitive.tests.PrimitiveFrontTester;
import au.edu.uts.eng.remotelabs.rigclient.rig.tests.AbstractControlledRigTester;
import au.edu.uts.eng.remotelabs.rigclient.rig.tests.AbstractRigTester;
import au.edu.uts.eng.remotelabs.rigclient.rig.tests.ConfiguredControlledRigTester;
import au.edu.uts.eng.remotelabs.rigclient.rig.tests.ConfiguredRigTester;
import au.edu.uts.eng.remotelabs.rigclient.type.tests.RigFactoryTester;
import au.edu.uts.eng.remotelabs.rigclient.util.tests.PropertiesConfigTester;

/**
 * Runs all the Rig Client unit tests.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    RigFactoryTester.class,
    ConfiguredRigTester.class,
    ConfiguredControlledRigTester.class,
    ConfiguredBatchRunnerTester.class,
    PrimitiveFrontTester.class,
    PrimitiveCacheTester.class,
    DirectoryCopierTester.class,
    DirectoryZipperTester.class,
    MacroSubstituerTester.class,
    AbstractBatchRunnerTester.class,
    AbstractControlledRigTester.class,
    AbstractRigTester.class,
    ConfiguredBatchRunnerTester.class,
    PropertiesConfigTester.class
})
public class RigClientTestSuite
{
    // Does nothing...
}
