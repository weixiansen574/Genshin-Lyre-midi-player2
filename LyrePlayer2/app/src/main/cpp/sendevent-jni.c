/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
#include <string.h>
#include <jni.h>

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <stdint.h>
#include <fcntl.h>
#include <sys/ioctl.h>
//#include <linux/input.h> // this does not compile
#include <errno.h>
#include <linux/time.h>
#include <unistd.h>

struct input_event {
    struct timeval time;
    __u16 type;
    __u16 code;
    __s32 value;
};

#define EVIOCGVERSION		_IOR('E', 0x01, int)			/* get driver version */
#define EVIOCGID		_IOR('E', 0x02, struct input_id)	/* get device ID */
#define EVIOCGKEYCODE		_IOR('E', 0x04, int[2])			/* get keycode */
#define EVIOCSKEYCODE		_IOW('E', 0x04, int[2])			/* set keycode */
#define EVIOCGNAME(len)		_IOC(_IOC_READ, 'E', 0x06, len)		/* get device name */
#define EVIOCGPHYS(len)		_IOC(_IOC_READ, 'E', 0x07, len)		/* get physical location */
#define EVIOCGUNIQ(len)		_IOC(_IOC_READ, 'E', 0x08, len)		/* get unique identifier */
#define EVIOCGKEY(len)		_IOC(_IOC_READ, 'E', 0x18, len)		/* get global keystate */
#define EVIOCGLED(len)		_IOC(_IOC_READ, 'E', 0x19, len)		/* get all LEDs */
#define EVIOCGSND(len)		_IOC(_IOC_READ, 'E', 0x1a, len)		/* get all sounds status */
#define EVIOCGSW(len)		_IOC(_IOC_READ, 'E', 0x1b, len)		/* get all switch states */
#define EVIOCGBIT(ev,len)	_IOC(_IOC_READ, 'E', 0x20 + ev, len)	/* get event bits */
#define EVIOCGABS(abs)		_IOR('E', 0x40 + abs, struct input_absinfo)		/* get abs value/limits */
#define EVIOCSABS(abs)		_IOW('E', 0xc0 + abs, struct input_absinfo)		/* set abs value/limits */
#define EVIOCSFF		_IOC(_IOC_WRITE, 'E', 0x80, sizeof(struct ff_effect))	/* send a force effect to a force feedback device */
#define EVIOCRMFF		_IOW('E', 0x81, int)			/* Erase a force effect */
#define EVIOCGEFFECTS		_IOR('E', 0x84, int)			/* Report number of effects playable at the same time */
#define EVIOCGRAB		_IOW('E', 0x90, int)			/* Grab/Release device */
// end <linux/input.h>


JNIEXPORT jint JNICALL Java_top_weixiansen574_lyreplayer2_ShizukuInputService_sendEventJNI(JNIEnv *env, jobject obj, jstring device, jint type, jint code, jint value) {
    const char *device_path = (*env)->GetStringUTFChars(env, device, 0);
    if (device_path == NULL) {
        fprintf(stderr, "Failed to get device path string\n");
        return -1;
    }

    int fd = open(device_path, O_RDWR);
    if (fd < 0) {
        fprintf(stderr, "could not open %s, %s\n", device_path, strerror(errno));
        (*env)->ReleaseStringUTFChars(env, device, device_path);
        return -1;
    }

    struct input_event event;
    memset(&event, 0, sizeof(event));
    event.type = type;
    event.code = code;
    event.value = value;

    ssize_t ret = write(fd, &event, sizeof(event));
    close(fd);

    if (ret < (ssize_t) sizeof(event)) {
        fprintf(stderr, "write event failed, %s\n", strerror(errno));
        (*env)->ReleaseStringUTFChars(env, device, device_path);
        return -1;
    }

    (*env)->ReleaseStringUTFChars(env, device, device_path);
    return 0;
}
