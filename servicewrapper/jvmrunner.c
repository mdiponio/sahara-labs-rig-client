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

#include "jvmrunner.h"

/**
 * Loads the configuration file, looking for the properties:
 *  * JVM_Location - 1      - Path to the Java virtual machine library.
 *  * Extra_Lib    - 0 .. * - List of extra JAR libraries to load to add to
 *                            classpath.
 * 
 * @return true if successful, false otherwise
 */
int loadConfig(void)
{
	char buf[201], prop[201], *line, *val;
	FILE *config, *jvmPath;

	memset(buf, 0, 201);

	if ((config = fopen(CONFIG_FILE, "r")) == NULL)
	{
		logMessage("Unable to open configuration file %s.\n", CONFIG_FILE);
		perror("Failed to open configuration file because");
		return 0;
	}
	logMessage("Opened log file '%s' successfully.\n", CONFIG_FILE);

	while (fgets(buf, 200, config) != NULL)
	{
		line = buf;
		memset(prop, 0, 201);

		line = trim(line);
		if (strlen(line) == 0) continue; /* Empty line.    */
		if (line[0] == '#') continue;    /* Comment line. */

		val = line;
		while (!isspace(*val) && *val != '\0') val++;
		if (*val == '\0')
		{
			logMessage("No value for prop %s.\n", line);
			continue;
		}

		strncpy(prop, line, val - line);
		val = val + 1;
		logMessage("Prop=%s Value=%s\n", prop, val);

		if (strcmp("JVM_Location", prop) == 0)
		{
			jvmSo = (char *)malloc(sizeof(char) * strlen(val));
			memset(jvmSo, 0, strlen(val));
			strcpy(jvmSo, val);
		}
		else if (strcmp("Extra_Lib", prop) == 0)
		{
			if (classPathExt == NULL)
			{
				classPathExt = (char *)malloc(strlen(val) + 1);
				memset(classPathExt, 0, strlen(val) + 1);
				strcat(classPathExt, val);
			}
			else
			{
				char *tmp = (char *)malloc(strlen(val) + 1 + strlen(classPathExt));
				strcpy(tmp, classPathExt);
				strcat(tmp, CLASS_PATH_DELIM);
				strcat(tmp, val);
				free(classPathExt);
				classPathExt = tmp;
			}
		}
		else
		{
			logMessage("Unknown property %s.\n", prop);
		}
	}

#ifdef WIN32
	/* If on Windows attempt to use the registry to determine if Java is installed, 
	 * what version and where the runtime DLL is installed. The keys used are:
	 *    - [HKEY_LOCAL_MACHINE\SOFTWARE\JavaSoft\Java Runtime Environment\CurrentVersion]
	 *    - [HKEY_LOCAL_MACHINE\SOFTWARE\JavaSoft\Java Runtime Environment\1.6\RuntimeLib] */
	if (jvmSo == NULL)
	{
		HKEY regKey;
		char regData[FILENAME_MAX], errMessage[255];
		DWORD regDataSize = FILENAME_MAX, err;
		float version;

		if ((err = RegOpenKeyEx(HKEY_LOCAL_MACHINE, "SOFTWARE\\JavaSoft\\Java Runtime Environment", 0, KEY_READ, &regKey)) == ERROR_SUCCESS &&
			(err = RegQueryValueEx(regKey, "CurrentVersion", NULL, NULL, regData, &regDataSize)) == ERROR_SUCCESS)
		{
			RegCloseKey(regKey);

			version = atof(regData);
			if (atof(regData) >= 1.6)
			{
				/* Version is installed and OK, so now need to find the RuntimeDLL location. */
				regDataSize = FILENAME_MAX;
				if ((err = RegOpenKeyEx(HKEY_LOCAL_MACHINE, "SOFTWARE\\JavaSoft\\Java Runtime Environment\\1.6", 0, KEY_READ, &regKey)) == ERROR_SUCCESS &&
					(err = RegQueryValueEx(regKey, "RuntimeLib", NULL, NULL, regData, &regDataSize)) == ERROR_SUCCESS &&
					regDataSize > 0)
				{
					regData[regDataSize + 1] = '\0';
					jvmSo = (char *)malloc(strlen(regData));
					strcpy(jvmSo, regData);
					logMessage("The detected Java runtime library location is '%s'.\n", jvmSo);
					RegCloseKey(regKey);
				}
			}
			else
			{
				logMessage("The version of Java you have installed (%.2f) is too old. It must be at least version 1.6.", atof(regData));
			}
		}
		else
		{
			FormatMessage(FORMAT_MESSAGE_FROM_SYSTEM, FORMAT_MESSAGE_FROM_HMODULE, err, 0, errMessage, 255, NULL);
			logMessage("Failed to read registry key (HKEY_LOCAL_MACHINE\\SOFTWARE\\JavaSoft\\Java Runtime Environment"
				"\\CurrentVersion). %s", errMessage);
		}
	}
#endif

	/* The Java installation path is mandatory so ensure it was properly loaded or detected. */
	if (jvmSo == NULL)
	{
		logMessage("The JVM location was not found.");
		return 0;
	}
	else if ((jvmPath = fopen(jvmSo, "r")) == NULL)
	{
		logMessage("Unable to use configured JVM location '%s' as the file does not exist.\n", val);
		perror("Failed opening JVM library");
		free(jvmSo);
		jvmSo = NULL;
		return 0;
	}

	logMessage("Using '%s' as the JVM to load.\n", jvmSo);
	fclose(jvmPath);
	return 1;
}

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
int generateClassPath(void)
{
	char currentDir[FILENAME_MAX];

	memset(currentDir, 0, FILENAME_MAX);
	if (getCWDir(currentDir, sizeof(currentDir)) == NULL)
	{
		logMessage("Unable to detect current working directory, failing...\n");
		return 0;
	}

	classPath = (char *)malloc(strlen(CLASS_PATH) + strlen(currentDir) + 1 + strlen(JAR_FILE));
	strcpy(classPath, CLASS_PATH);

	/* Rig Client library. */
	strcat(classPath, currentDir);
	strcat(classPath, "/");
	strcat(classPath, JAR_FILE);

	/* Configured extension libraries. */
	if (classPathExt != NULL)
	{
		classPath = (char *)realloc(classPath, strlen(classPath) + 1 + strlen(classPathExt));
		strcat(classPath, CLASS_PATH_DELIM);
		strcat(classPath, classPathExt);
	}

#ifdef WIN32
	{
		HANDLE fileHandle;
		WIN32_FIND_DATA fileData;
		DWORD size = strlen(classPath), oldSize;

		if ((fileHandle = FindFirstFile("lib/*.jar", &fileData)) != INVALID_HANDLE_VALUE)
		{
			do
			{
				oldSize = strlen(classPath);
				size += 7 + strlen(currentDir) + strlen(fileData.cFileName);
				if ((classPath = (char *)realloc(classPath, size)) == NULL)
				{
					printf("realloc failed\n");
				}
				classPath[oldSize] = '\0';

				strcat(classPath, CLASS_PATH_DELIM);
				strcat(classPath, currentDir);
				strcat(classPath, "/lib/");
				strcat(classPath, fileData.cFileName);
			}
			while (FindNextFile(fileHandle, &fileData));
			FindClose(fileHandle);
		}
	}
#else
	{
		DIR *dir;
		struct dirent *dp;
		size_t size = strlen(classPath), oldSize;

		if (!(dir = opendir("lib/")))
		{
			logMessage("Unable to open directory '%s/lib'. Not adding JARs in that directory.\n", currentDir);
			perror("Unable to open 'lib/'");
		}
		else
		{
			while (dp = readdir(dir))
			{
				if (strstr(dp->d_name, ".jar") == (dp->d_name + strlen(dp->d_name) - 4))
				{
					logMessage("Adding JAR with path %s.\n", dp->d_name);
					
					oldSize = strlen(classPath);
					size += 7 + strlen(currentDir) + strlen(dp->d_name);
					classPath = (char *)realloc(classPath, size);
					classPath[oldSize] = '\0';

					strcat(classPath, CLASS_PATH_DELIM);
					strcat(classPath, currentDir);
					strcat(classPath, "/lib/");
					strcat(classPath, dp->d_name);
				}
			}
		}
	}
#endif

	logMessage("Class path argument for Java virtual machine is '%s'.\n", classPath);
	return 1;
}

/**
 * Starts the Java virual machine.
 *
 * @return true if successful, false otherwise
 */
int startJVM()
{
	JavaVMInitArgs vm_args;
	JavaVMOption options[2];
	jint res;
	JNIEnv *env;
	jclass clazz;
	jmethodID method;

#ifdef WIN32
	HINSTANCE hVM;
#else
	void *libVM;
#endif

	/* Set the classpath. */
	options[0].optionString = classPath;
	options[1].optionString = "-Xrs";
	vm_args.options = options;
	vm_args.nOptions = 2;
	vm_args.ignoreUnrecognized = JNI_FALSE;
	vm_args.version = JNI_VERSION_1_4;

	/* Load the JVM library and find the JNI_CreateJavaVM function. */
#ifdef WIN32
	hVM = LoadLibrary(jvmSo);
	if (hVM == NULL)
	{
		logMessage("Unable to load library %s.\n", jvmSo);
		return 0;
	}
	createJVM = (CreateJavaVM)GetProcAddress(hVM, "JNI_CreateJavaVM");
#else
	libVM = dlopen(jvmSo, RTLD_LAZY);
	if (libVM == NULL)
	{	
		logMessage("Unable to load library %s.\n", jvmSo);
		perror("Error loading JVM library");
		return 0;
	}
	createJVM = (CreateJavaVM)dlsym(libVM, "JNI_CreateJavaVM");
#endif

	/* Create the JVM. */
	res = createJVM(&vm, (void **)&env, &vm_args);
	if (res < 0)
	{
		logMessage("Failed to create JVM, response code is %i.\n", res);
		return 0;
	}
	logMessage("Successfully created Java virtual machine.\n");

	/* Find the start up class. */
	if ((clazz = (*env)->FindClass(env, CLASS_NAME)) == NULL)
	{
		logMessage("Unable to find class %s.\n", CLASS_NAME);
		return 0;
	}

	/* Find start up method and invoke it. */
	if ((method = (*env)->GetStaticMethodID(env, clazz, STARTUP_METHOD, "()V")) == NULL)
	{
		logMessage("Unable to find method %s in class %s.\n", STARTUP_METHOD, CLASS_NAME);
		return 0;
	}
	(*env)->CallStaticVoidMethod(env, clazz, method, NULL);

	if ((*env)->ExceptionCheck(env))
	{
		logMessage("Exception thrown starting up rig client (called method %s on %s).\n", STARTUP_METHOD, CLASS_NAME);
		return 0;
	}

	return 1;
}

/**
 * Shuts down the Java virtual machine.
 *
 * @return true if successful, false otherwise
 */
int shutDownJVM()
{
	JNIEnv *env;
	jclass clazz;
	jmethodID method;

	(*vm)->AttachCurrentThread(vm, (void **)&env, NULL);

	clazz = (*env)->FindClass(env, CLASS_NAME);
	if ((method = (*env)->GetStaticMethodID(env, clazz, SHUTDOWN_METHOD, "()V")) == NULL)
	{
		logMessage("Unable to find shutdown method %s in class %s.\n", SHUTDOWN_METHOD, CLASS_NAME);
		return 0;
	}

	printf("Calling shutdown...\n");
	(*env)->CallStaticVoidMethod(env, clazz, method, NULL);
	if ((*env)->ExceptionCheck(env))
	{
		logMessage("Exception thrown shutting down rig client.\n");
		return 0;
	}

	return 1;
}
/**
 * Logs messages to a file. This function has the same format as
 * 'printf'.
 *
 * @param *fmt format string
 * @param ... format arguments
 */
void logMessage(char *fmt, ...)
{
	static FILE* logFile;
	va_list argp;

	if (logFile == NULL)
	{
		logFile = fopen(LOG_FILE, "a");
		if (logFile == NULL)
		{
			printf("Unable to open log file %s.", LOG_FILE);
			printf("Unable to log %s.", fmt);
		}
	}

	va_start(argp, fmt);
	vfprintf(logFile, fmt, argp);
	va_end(argp);
}

/** 
 * Trims leading and trailing whitespace from a string.
 * 
 * @param *tmp string to trim
 */
char *trim(char *tmp)
{
	char *end;

	/* Trim leading whitespace. */
	while (*tmp != '\0' && isspace(*tmp)) tmp++;

	/* Trim trailing whitespace. */
	end = tmp + strlen(tmp) - 1;
	while (end > tmp && isspace(*end)) end--;
	*(end + 1) = '\0';

	return tmp;
}
