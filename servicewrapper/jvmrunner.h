/**
 * Sahara Rig Client - Service Wrapper
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
 * @date 8th March 2010
 */

#ifndef JVMRunner_H
#define JVMRunner_H

#ifdef __cplusplus
extern "C"
{
#endif

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <stdarg.h>
#include <time.h>

#ifdef WIN32
#define _CRT_SECURE_NO_WARNINGS 1
#include <windows.h>
#include <winbase.h>
#include <direct.h>
#define getCWDir  _getcwd
#else
#include <dirent.h>
#include <dlfcn.h>
#include <unistd.h>
#define getCWDir getcwd
#endif

#include "jni.h"

/******************************************************************************
 ** Constants                                                                **
 *****************************************************************************/

/** Java class to call start up and shutdown methods from. */
#define CLASS_NAME "au/edu/uts/eng/remotelabs/rigclient/main/RigClient"

/** Java start up method. */
#define STARTUP_METHOD "start"

/** Java shutdown method. */
#define SHUTDOWN_METHOD "stop"

/** Java JVM classpath option. */
#define CLASS_PATH "-Djava.class.path="

#ifdef WIN32
#define CLASS_PATH_DELIM ";"
#else
#define CLASS_PATH_DELIM ":"
#endif

/** JAR Java class is contained in. */
#define JAR_FILE "rigclient.jar"

/** Log file for Service. */
#define LOG_FILE "rigclientservice.log"

/** Configuration file containing location. */
#define WD_CONFIG_FILE "rigclient_service.ini"
#define SI_CONFIG_FILE "config/rigclient_service.ini"
#define IN_CONFIG_FILE "conf/rigclient_service.ini"

/** Name of the environment variable which may contain the JVM shared object
 *  location. */
#define JVM_SO_ENV "JVM_SO"

/******************************************************************************
 ** Global variables.                                                        **
 *****************************************************************************/

/** JVM DLL. */
char *jvmSo;

/** Java Class Path. */
char *classPath;
char *classPathExt;

/** The list of system properties. */
int numSysProps;
char *sysProps[256];

/** Current (executable) directory. */
char *currentDir;

/** Java virtual machine. */
JavaVM *vm;

/** Function declaration to create a Java Virtual Machine. */
typedef jint (JNICALL *CreateJavaVM)(JavaVM **vm, void **env, void *args);

/** Function pointer to create a VM. */
CreateJavaVM createJVM;

/** Max memory for Java virtual machine. */
char *maxHeap;

/** Start up and shutdown times. */
time_t startTime;
time_t stopTime;

/******************************************************************************
 ** Function Prototypes                                                      **
 *****************************************************************************/

/**
 * Loads the configuration file, looking for the properties:
 *  * JVM_Location - 1      - Path to the Java virtual machine library.
 *  * Extra_Lib    - 0 .. * - List of extra JAR libraries to load to add to
 *                            classpath.
 *
 * @return true if successful, false otherwise
 */
int loadConfig(void);

/**
 * Generates the classpath argument to provide to the java virtual machine.
 * The classpath contains:
 *
 * 1) The rig client jar which is in the current working directory.
 * 2) The configured JAR libraries.
 * 3) The JARs in the 'lib/' folder.
 *
 * @return true if successful
 */
int generateClassPath(void);

/**
 * Starts the Java virual machine.
 *
 * @return true if successful, false otherwise
 */
int startJVM();

/**
 * Shuts down the Java virtual machine.
 *
 * @return true if successful, false otherwise
 */
int shutDownJVM();

/**
 * Logs messages to a file. This function has the same format as
 * 'printf'.
 *
 * @param *fmt format string
 * @param ... format arguments
 */
void logMessage(char *fmt, ...);

/**
 * Trims leading and trailing whitespace from a string.
 *
 * @param *tmp string to trim
 * @return trimmed string
 */
char *trim(char *tmp);

#ifdef __cplusplus
}
#endif

#endif
