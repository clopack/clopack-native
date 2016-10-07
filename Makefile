CC=		gcc
CFLAGS=		-ansi -pedantic -Wall -Werror -Wextra -c -fpic

SRC_BSD=	src/clopack_native/dev_bsd.c
OBJ_BSD=	dev_bsd.o

LIB=		lib/libclopack_native.so

bsd: $(SRC_BSD)
	$(CC) $(CFLAGS) $(SRC_BSD)
	mkdir -p lib/
	$(CC) -shared -o $(LIB) $(OBJ_BSD)

.PHONY: clean

clean:
	rm -f $(OBJ_BSD)
	rm -f $(LIB)
