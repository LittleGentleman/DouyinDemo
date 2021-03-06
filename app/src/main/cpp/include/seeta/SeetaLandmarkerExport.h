
#ifndef SEETA_LANDMARKER_API_H
#define SEETA_LANDMARKER_API_H

#ifdef SEETAFACELANDMARKER_STATIC_DEFINE
#  define SEETA_LANDMARKER_API
#  define SEETAFACELANDMARKER_NO_EXPORT
#else
#  ifndef SEETA_LANDMARKER_API
#    ifdef SeetaFaceLandmarker_EXPORTS
        /* We are building this library */
#      define SEETA_LANDMARKER_API __attribute__((visibility("default")))
#    else
        /* We are using this library */
#      define SEETA_LANDMARKER_API __attribute__((visibility("default")))
#    endif
#  endif

#  ifndef SEETAFACELANDMARKER_NO_EXPORT
#    define SEETAFACELANDMARKER_NO_EXPORT __attribute__((visibility("hidden")))
#  endif
#endif

#ifndef SEETAFACELANDMARKER_DEPRECATED
#  define SEETAFACELANDMARKER_DEPRECATED __attribute__ ((__deprecated__))
#endif

#ifndef SEETAFACELANDMARKER_DEPRECATED_EXPORT
#  define SEETAFACELANDMARKER_DEPRECATED_EXPORT SEETA_LANDMARKER_API SEETAFACELANDMARKER_DEPRECATED
#endif

#ifndef SEETAFACELANDMARKER_DEPRECATED_NO_EXPORT
#  define SEETAFACELANDMARKER_DEPRECATED_NO_EXPORT SEETAFACELANDMARKER_NO_EXPORT SEETAFACELANDMARKER_DEPRECATED
#endif

#if 0 /* DEFINE_NO_DEPRECATED */
#  ifndef SEETAFACELANDMARKER_NO_DEPRECATED
#    define SEETAFACELANDMARKER_NO_DEPRECATED
#  endif
#endif

#endif /* SEETA_LANDMARKER_API_H */
