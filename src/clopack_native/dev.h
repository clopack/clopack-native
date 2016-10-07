#ifndef _CLOPACK_NATIVE_DEV_H_
#define _CLOPACK_NATIVE_DEV_H_

/*
 * Opens a packet device/socket on the given interface.
 * Returns a valid file descriptor on success, -1 otherwise.
 */
int dev_open(const char *);

/*
 * Reads n bytes to a buffer from a packet device/socket.
 * Returns the number of bytes read on success, -1 otherwise.
 */
int dev_read(int, void *, int);

/*
 * Writes n bytes from a buffer through a packet device/socket.
 * Returns the number of bytes written on success, -1 otherwise.
 */
int dev_write(int, void *, int);

/*
 * Closes a packet device/socket.
 */
void dev_close(int);

/*
 * Returns the required read buffer length of the device.
 */
int dev_buf_len(void);

#endif
