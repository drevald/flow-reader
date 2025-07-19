#include "mylib.h"
#include "common.h"

double atof(const char *nptr) {
    return (strtod(nptr, NULL));
}
