// Stub implementations for API < 23 functions needed by precompiled libraries
// The precompiled libraries (OpenCV, Leptonica) need these

#include <stdio.h>
#include <string.h>
#include <sys/mman.h>
#include <unistd.h>

// Forward declarations to avoid including stdlib.h (which has inline atof)
extern "C" {
    double strtod(const char *nptr, char **endptr);
    extern int errno;
}

#define ENOSYS 38
#define MAP_FAILED ((void *) -1)

extern "C" {
    // Export atof for precompiled libraries (needed for API < 21)
    double atof(const char *nptr) {
        return strtod(nptr, (char**)0);
    }

    // Stub for open_memstream (API 23+)
    // Return NULL to indicate failure - calling code should handle this
    FILE* open_memstream(char **ptr, size_t *sizeloc) {
        errno = ENOSYS; // Function not implemented
        return NULL;
    }

    // Stub for fmemopen (API 23+)
    // Return NULL to indicate failure - calling code should handle this
    FILE* fmemopen(void *buf, size_t *sizeloc, const char *mode) {
        errno = ENOSYS; // Function not implemented
        return NULL;
    }

    // Stub for mmap64 (API 21+)
    // Use regular mmap as fallback for API < 21
    void* mmap64(void *addr, size_t length, int prot, int flags, int fd, off_t offset) {
        // On 32-bit systems with API < 21, just use regular mmap
        // This may fail for files > 2GB but that's unlikely for this use case
        return mmap(addr, length, prot, flags, fd, (off_t)offset);
    }

    // Export rand/srand for precompiled libraries
    // Simple LCG (Linear Congruential Generator) implementation
    // Using parameters from glibc: a=1103515245, c=12345, m=2^31
    static unsigned int __rand_seed = 1;

    int rand(void) {
        __rand_seed = __rand_seed * 1103515245 + 12345;
        return (__rand_seed >> 16) & 0x7fff;  // Return 15-bit value (0 to 32767)
    }

    void srand(unsigned int seed) {
        __rand_seed = seed;
    }
}
