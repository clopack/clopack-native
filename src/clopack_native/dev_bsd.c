#include <sys/types.h>
#include <sys/socket.h>
#include <sys/ioctl.h>
#include <net/bpf.h>
#include <net/if.h>
#include <net/if_dl.h>

#include <err.h>
#include <errno.h>
#include <fcntl.h>
#include <string.h>
#include <unistd.h>

#include "dev.h"

/* Possible BPF device paths. */
static const char *bpf_dev_paths[] = {
    "/dev/bpf",
    "/dev/bpf0",
    "/dev/bpf1",
    "/dev/bpf2",
    "/dev/bpf3",
    "/dev/bpf4",
    "/dev/bpf5",
    "/dev/bpf6",
    "/dev/bpf7",
    "/dev/bpf8",
    "/dev/bpf9",
    NULL
};

/* BPF read buffer length. */
#define BPF_BUF_LEN 32768

/* Datalink type buffer length. */
#define DLT_BUF_LEN 64

int
dev_open(const char *iface)
{
	int		  dev;
	const char	**pathp;
	struct		  ifreq ifr;
	u_int		  dlt_buf[DLT_BUF_LEN];
	struct		  bpf_dltlist dlt_list;
	u_int		  dlt;
	u_int		  imm;

	unsigned int	  i;

	/* Open the first available BPF device file descriptor. */
	for (pathp = bpf_dev_paths; *pathp != NULL; pathp++) {
		dev = open(*pathp, O_RDWR);
		if (dev != -1)
			break;
	}

	if (dev == -1) {
		warn("failed to open BPF device");
		return -1;
	}

	/* Set the BPF device interface. */
	strlcpy(ifr.ifr_name, iface, sizeof(ifr.ifr_name));
	if (ioctl(dev, BIOCSETIF, &ifr) == -1)
		goto err;

	/*
	 * Retrieve the available datalink types for the interface
	 * and select one (if found) that is supported by linkcat.
	 */
	dlt_list.bfl_len = DLT_BUF_LEN;
	dlt_list.bfl_list = dlt_buf;
	if (ioctl(dev, BIOCGDLTLIST, &dlt_list) == -1) {
		warn("failed to retrieve available datalink types");
		goto err;
	}

	dlt = 0;
	for (i = 0; i < dlt_list.bfl_len; i++) {
		dlt = dlt_list.bfl_list[i];
		switch (dlt) {
		case DLT_EN10MB:
		case DLT_IEEE802_11:
			break;
		}
	}

	if (dlt == 0) {
		warnx("no suitable datalink type found");
		goto err;
	}

	if (ioctl(dev, BIOCSDLT, &dlt) == -1) {
		warn("failed to set datalink type for BPF device");
		goto err;
	}

	/* Enable immediate reads for real-time data acknowledgement. */
	imm = 1;
	if (ioctl(dev, BIOCIMMEDIATE, &imm) == -1) {
		warn("failed to set immediate mode on BPF device");
		goto err;
	}

	/* Lock the BPF device. */
	if (ioctl(dev, BIOCLOCK) == -1) {
		warn("failed to lock BPF device");
		goto err;
	}

	return dev;

err:
	(void)close(dev);

	return -1;
}

int
dev_read(int dev, void *buf, int len)
{
	ssize_t nr;

	do {
		nr = read(dev, buf, len);
		if (nr == -1 && errno == EINTR)
			continue;
	} while (nr == 0);

	if (nr == -1) {
		warn("failed to read data from BPF device");
		return -1;
	}

	return (int)nr;
}

int
dev_write(int dev, void *buf, int len)
{
	ssize_t nw;

	do
		nw = write(dev, buf, len);
	while (nw == -1 && errno == EINTR);

	if (nw == -1) {
		warn("failed to write data through BPF device");
		return -1;
	}

	return (int)nw;
}

void
dev_close(int dev)
{
	(void)close(dev);
}

int
dev_buf_len(void)
{
	return BPF_BUF_LEN;
}
