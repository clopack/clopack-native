CC=		gcc
CFLAGS=		-g -ansi -pedantic -Wall -Werror -Wextra -c -fpic

SRC_BSD=	src/clopack_native/dev_bsd.c
OBJ_BSD=	dev_bsd.o

LIB=		libclopack_native.so

bsd: $(SRC_BSD)
	$(CC) $(CFLAGS) $(SRC_BSD)
	mkdir -p lib/
	$(CC) -shared -o resources/$(LIB) $(OBJ_BSD)

.PHONY: clean

clean:
	rm -f $(OBJ_BSD)
	rm -f resources/$(LIB)
