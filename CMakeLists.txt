# CMake configuration file for CODA.
#
project(CODA)

set(VERSION 2.18.3)
set(CODA_VERSION \"${VERSION}\")

# General setup
#
cmake_minimum_required(VERSION 2.6.0)
set(CMAKE_MODULE_PATH
  ${CODA_SOURCE_DIR}/CMakeModules
  ${CMAKE_MODULE_PATH}
  CACHE INTERNAL "Local CMake modules")
include(CheckFunctionExists)
include(CheckLibraryExists)
include(CheckIncludeFile)
include(CheckSymbolExists)
include(CheckTypeSize)
include(TestBigEndian)
include(CMakeDependentOption)

# Use RelWithDebInfo as default target 
#
IF(NOT CMAKE_BUILD_TYPE)
  SET(CMAKE_BUILD_TYPE RelWithDebInfo CACHE STRING
      "Choose the type of build, options are: None Debug Release RelWithDebInfo MinSizeRel."
      FORCE)
ENDIF(NOT CMAKE_BUILD_TYPE)

# Option handling
#
option(CODA_BUILD_SUBPACKAGE_MODE "build CODA as sub-package" OFF)
option(CODA_BUILD_IDL "build IDL interface" OFF)
option(CODA_BUILD_MATLAB "build MATLAB interface" OFF)
option(CODA_BUILD_PYTHON "build Python interface" OFF)
option(CODA_WITH_HDF4 "use HDF4" OFF)
option(CODA_WITH_HDF5 "use HDF5" OFF)
cmake_dependent_option(
  CODA_ENABLE_HDF4_VDATA_ATTRIBUTES "enable HDF4 Vdata attributes" ON
  CODA_WITH_HDF4 ON)

# The following is done because in CMake options are always
# ON/OFF only, but for config.h substition we need something with
# value 1.
#
if(CODA_ENABLE_HDF4_VDATA_ATTRIBUTES)
  set(ENABLE_HDF4_VDATA_ATTRIBUTES 1)
endif(CODA_ENABLE_HDF4_VDATA_ATTRIBUTES)

# Some autoconf variables that CMake does not use itself, but
# that are used in the config file substitutions.
#
set(PACKAGE_NAME \"${CMAKE_PROJECT_NAME}\")
string(TOLOWER ${PACKAGE_NAME} PACKAGE)
set(PACKAGE_VERSION ${CODA_VERSION})
string(REPLACE "\"" "" PACKAGE_STRING "${PACKAGE_NAME} ${PACKAGE_VERSION}")
set(PACKAGE_STRING \"${PACKAGE_STRING}\")
set(VERSION \"${VERSION}\")
set(PACKAGE_TARNAME ${PACKAGE})
set(PACKAGE_BUGREPORT \"\")

# Global compilation settings
#
if(CMAKE_COMPILER_IS_GNUCC)
  set(GNU_WARNING_FLAGS "-W -Wall -Wextra")
  set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} ${GNU_WARNING_FLAGS}")
  if("${CMAKE_SYSTEM_PROCESSOR}" STREQUAL "x86_64")
    set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -fPIC")
  endif("${CMAKE_SYSTEM_PROCESSOR}" STREQUAL "x86_64")
endif(CMAKE_COMPILER_IS_GNUCC)

# hdf
#
if(CODA_WITH_HDF4)

  find_package(HDF4)

  if(NOT HDF4_FOUND)
    message(FATAL_ERROR "HDF4 libraries and/or header files are not found. Try setting the HDF4_LIBRARY_DIR and HDF4_INCLUDE_DIR cmake variables to the location of your HDF4 library and include files.")
  else(NOT HDF4_FOUND)
    set(HAVE_HDF4 1)
    include_directories(${HDF4_INCLUDE_DIR})
  endif(NOT HDF4_FOUND)

endif(CODA_WITH_HDF4)


if(CODA_WITH_HDF5)

  find_package(HDF5)

  if(NOT HDF5_FOUND)
    message(FATAL_ERROR "HDF5 libraries and/or header files are not found. Try setting the HDF5_LIBRARY_DIR and HDF5_INCLUDE_DIR cmake variables to the location of your HDF5 library and include files.")
  else(NOT HDF5_FOUND)
    set(HAVE_HDF5 1)
    include_directories(${HDF5_INCLUDE_DIR})
  endif(NOT HDF5_FOUND)

endif(CODA_WITH_HDF5)


# *** xml ***
#
set(XML_NS 1)
set(XML_DTD 1)
set(XML_LARGE_SIZE 1)
set(XML_CONTEXT_BYTES 1024)
set(CODA_EXPAT_NAME_MANGLE 1)


# Lex / Yacc
find_package(BISON)
find_package(FLEX)

BISON_TARGET(coda-expr-parser ${CMAKE_CURRENT_SOURCE_DIR}/libcoda/coda-expr-parser.y ${CMAKE_CURRENT_BINARY_DIR}/libcoda/coda-expr-parser.c)
FLEX_TARGET(coda-expr-tokenizer ${CMAKE_CURRENT_SOURCE_DIR}/libcoda/coda-expr-tokenizer.l ${CMAKE_CURRENT_BINARY_DIR}/lex.yy.c)
add_flex_bison_dependency(coda-expr-tokenizer coda-expr-parser)

# Required include files
#
set(INCLUDES "")

macro(find_include FILE VARIABLE)
  CHECK_INCLUDE_FILE(${FILE} ${VARIABLE})
  if(${VARIABLE})
    set(INCLUDES ${INCLUDES} ${FILE})
  endif(${VARIABLE})
endmacro(find_include)

find_include(dirent.h HAVE_DIRENT_H)
find_include(dlfcn.h HAVE_DLFCN_H)
find_include(export.h HAVE_EXPORT_H)
find_include(inttypes.h HAVE_INTTYPES_H)
find_include(memory.h HAVE_MEMORY_H)
find_include(stdint.h HAVE_STDINT_H)
find_include(stdlib.h HAVE_STDLIB_H)
find_include(strings.h HAVE_STRINGS_H)
find_include(string.h HAVE_STRING_H)
find_include(sys/mman.h HAVE_SYS_MMAN_H)
find_include(sys/socket.h HAVE_SYS_SOCKET_H)
find_include(sys/stat.h HAVE_SYS_STAT_H)
find_include(sys/types.h HAVE_SYS_TYPES_H)
find_include(unistd.h HAVE_UNISTD_H)

set(CMAKE_EXTRA_INCLUDE_FILES ${INCLUDES})

# Required functions
#
check_function_exists(bcopy HAVE_BCOPY)
check_function_exists(floor HAVE_FLOOR)
check_function_exists(getpagesize HAVE_GETPAGESIZE)
check_function_exists(malloc HAVE_MALLOC)
check_function_exists(memmove HAVE_MEMMOVE)
check_function_exists(mmap HAVE_MMAP)
check_function_exists(pread HAVE_PREAD)
check_function_exists(realloc HAVE_REALLOC)
check_function_exists(stat HAVE_STAT)
check_function_exists(strcasecmp HAVE_STRCASECMP)
check_function_exists(strdup HAVE_STRDUP)
check_function_exists(strerror HAVE_STRERROR)
check_function_exists(strncasecmp HAVE_STRNCASECMP)
check_function_exists(vsnprintf HAVE_VSNPRINTF)

if(WIN32)
  # Hardcode settings for Windows
  set(HAVE_STRDUP 1)
  set(HAVE_STRCASECMP 1)
  set(HAVE_STRNCASECMP 1)
  set(HAVE_VSNPRINTF 1)
endif(WIN32)


# Required types (and their sizes)
#
check_symbol_exists(uchar "${INCLUDES}" HAVE_UCHAR)
set(UCHAR "unsigned char")
check_symbol_exists(size_t "${INCLUDES}" HAVE_SIZE_T)
set(SIZE_T "long")
check_symbol_exists(ptrdiff_t "${INCLUDES}" HAVE_PTRDIFF_T)
set(PTRDIFF_T "int")

check_type_size(size_t SIZEOF_SIZE_T)
check_type_size(off_t SIZEOF_OFF_T)
check_type_size(double SIZEOF_DOUBLE)
check_type_size(float SIZEOF_FLOAT)
check_type_size(int SIZEOF_INT)
check_type_size(long SIZEOF_LONG)
check_type_size(short SIZEOF_SHORT)

test_big_endian(WORDS_BIGENDIAN)

if(HAVE_STDLIB_H AND HAVE_STDDEF_H)
  set(STDC_HEADERS 1)
endif(HAVE_STDLIB_H AND HAVE_STDDEF_H)
if(NOT HAVE_SYS_TYPES_H)
  set(NO_SYS_TYPES_H 1)
endif(NOT HAVE_SYS_TYPES_H)
if(NOT HAVE_STRERROR)
  set(NO_STRERROR 1)
endif(NOT HAVE_STRERROR)

set(CODA_INCLUDE_DIRS
  ${CMAKE_CURRENT_SOURCE_DIR}/libcoda
  ${CMAKE_CURRENT_BINARY_DIR}
  ${CMAKE_CURRENT_BINARY_DIR}/libcoda
  CACHE INTERNAL "Include directories for the CODA library")

include_directories(${CODA_INCLUDE_DIRS})

# Required libraries
#
if(NOT WIN32)
# Only on Unix -- Windows does not need this, and the CMake
# people explicitly recommend just special-casing the platform
# through 'if'.
  find_library(LIBM_LIBRARY m)
  mark_as_advanced(LIBM_LIBRARY)
endif(NOT WIN32)

# The Doxyfile uses some autoconf variables CMake does not have.
# We set them here manually, so that we can use the same .in
# file for both autoconf and cmake
#
set(top_builddir ${CMAKE_BINARY_DIR})
set(top_srcdir ${CMAKE_SOURCE_DIR})
set(srcdir ${CMAKE_CURRENT_SOURCE_DIR}/libcoda/doc)
configure_file(
  ${CMAKE_CURRENT_SOURCE_DIR}/libcoda/doc/Doxyfile.in
  ${CMAKE_CURRENT_BINARY_DIR}/libcoda/doc/Doxyfile)


if(MSVC)
# Suppress warnings from VC on deprecated POSIX names
add_definitions(-D_CRT_NONSTDC_NO_DEPRECATE)
endif(MSVC)

# libcoda sources
#
set(LIBCODA_SOURCES
  libcoda/coda-ascbin-cursor.c
  libcoda/coda-ascbin.h
  libcoda/coda-ascii-cursor.c
  libcoda/coda-ascii-internal.h
  libcoda/coda-ascii.c
  libcoda/coda-ascii.h
  libcoda/coda-bin-cursor.c
  libcoda/coda-bin-internal.h
  libcoda/coda-bin.c
  libcoda/coda-bin.h
  libcoda/coda-cdf-cursor.c
  libcoda/coda-cdf-internal.h 
  libcoda/coda-cdf-type.c
  libcoda/coda-cdf.c
  libcoda/coda-cdf.h
  libcoda/coda-check.c
  libcoda/coda-cursor-read.c
  libcoda/coda-cursor.c
  libcoda/coda-definition-parse.c
  libcoda/coda-definition.c
  libcoda/coda-definition.h
  libcoda/coda-detection.c
  libcoda/coda-errno.c
  libcoda/coda-expr.c
  libcoda/coda-expr.h
  libcoda/coda-filefilter.c
  libcoda/coda-filefilter.h
  libcoda/coda-grib-cursor.c
  libcoda/coda-grib-internal.h
  libcoda/coda-grib-type.c
  libcoda/coda-grib.c
  libcoda/coda-grib.h
  libcoda/coda-internal.h
  libcoda/coda-mem-cursor.c
  libcoda/coda-mem-internal.h
  libcoda/coda-mem-type.c
  libcoda/coda-mem.c
  libcoda/coda-mem.h
  libcoda/coda-netcdf-cursor.c
  libcoda/coda-netcdf-internal.h
  libcoda/coda-netcdf-type.c
  libcoda/coda-netcdf.c
  libcoda/coda-netcdf.h
  libcoda/coda-path.h
  libcoda/coda-product.c
  libcoda/coda-read-array.h
  libcoda/coda-read-bits.h
  libcoda/coda-read-bytes.h
  libcoda/coda-read-bytes-in-bounds.h
  libcoda/coda-read-partial-array.h
  libcoda/coda-rinex.c
  libcoda/coda-rinex.h
  libcoda/coda-sp3.c
  libcoda/coda-sp3.h
  libcoda/coda-swap2.h
  libcoda/coda-swap4.h
  libcoda/coda-swap8.h
  libcoda/coda-time.c
  libcoda/coda-transpose-array.h
  libcoda/coda-tree.c
  libcoda/coda-tree.h
  libcoda/coda-type.c
  libcoda/coda-type.h
  libcoda/coda-utils.c
  libcoda/coda-xml-internal.h
  libcoda/coda-xml-parser.c
  libcoda/coda-xml.c
  libcoda/coda-xml.h
  libcoda/coda.c
  libcoda/hashtable.c
  libcoda/hashtable.h
  libcoda/ipow.h
  libcoda/ziparchive.c
  libcoda/ziparchive.h
  ${BISON_coda-expr-parser_OUTPUTS}
  ${FLEX_coda-expr-tokenizer_OUTPUTS})

set(LIBCODA_HDF4_FILES
  libcoda/coda-hdf4-cursor.c
  libcoda/coda-hdf4-internal.h
  libcoda/coda-hdf4-type.c
  libcoda/coda-hdf4.c
  libcoda/coda-hdf4.h)

if(CODA_WITH_HDF4)
  set(LIBCODA_SOURCES ${LIBCODA_SOURCES} ${LIBCODA_HDF4_FILES})
endif(CODA_WITH_HDF4)


set(LIBCODA_HDF5_FILES
  libcoda/coda-hdf5-cursor.c
  libcoda/coda-hdf5-internal.h
  libcoda/coda-hdf5-type.c
  libcoda/coda-hdf5.c
  libcoda/coda-hdf5.h)

if(CODA_WITH_HDF5)
  set(LIBCODA_SOURCES ${LIBCODA_SOURCES} ${LIBCODA_HDF5_FILES})
endif(CODA_WITH_HDF5)


if(NOT CODA_BUILD_SUBPACKAGE_MODE)

  # fortran

  set(fortran_DATA
    fortran/Makefile
    fortran/coda.inc
    fortran/coda_fortran.c
    fortran/example.f)

  set(fortran_SCRIPTS fortran/checkf77.sh)

  # idl
  
  if(CODA_BUILD_IDL)
  
    # If not in cache, try from environment
    if(NOT IDL_DIR)
      if($ENV{IDL_DIR} MATCHES ".+")
        file(TO_CMAKE_PATH $ENV{IDL_DIR} IDL_DIR)
      endif($ENV{IDL_DIR} MATCHES ".+")
    endif(NOT IDL_DIR)
  
    if(IDL_DIR)
      set(IDL_DIR ${IDL_DIR} CACHE STRING "Root directory of an IDL installation" FORCE)
      if(WIN32)
        set(IDL_INSTALL_DIR idl)
      else()
        set(IDL_INSTALL_DIR lib/idl)
      endif()
  
      if(WIN32)
        set(IDL_COMPILE_FLAGS "-nologo -I. -I${CODA_BINARY_DIR} -I${CODA_SOURCE_DIR}/libcoda -DWIN32_LEAN_AND_MEAN -DWIN32 -DHAVE_CONFIG_H -DIDL_V5_4")
        set(IDL_LIBDIR "${IDL_DIR}/bin/bin.x86/")
        set(IDL_LIB "${IDL_LIBDIR}/idl.lib")
        set(IDL_LINK_FLAGS "/EXPORT:IDL_Load,@1")
      else(WIN32)
        if(APPLE)
          set(IDL_LINK_FLAGS "${IDL_LINK_FLAGS} -undefined dynamic_lookup")
        endif(APPLE)
      endif(WIN32)
  
      set(IDL_INCLUDE_DIR "${IDL_DIR}/external")
      include_directories(${IDL_INCLUDE_DIR})
  
      set(CODA_IDL_SOURCES idl/coda-idl.c)
      set_source_files_properties(${CODA_IDL_SOURCES}
        PROPERTIES COMPILE_FLAGS "${COMPILE_FLAGS} ${IDL_COMPILE_FLAGS}")
      # Visual Studio does not like targets with a dash in the
      # name, hence coda_idl rather than coda-idl.
      add_library(coda_idl MODULE ${CODA_IDL_SOURCES})
      target_link_libraries(coda_idl coda_static ${HDF4_LIBRARIES} ${HDF5_LIBRARIES} ${IDL_LIB})
      set_target_properties(coda_idl
        PROPERTIES LINK_FLAGS "${IDL_LINK_FLAGS}"
        OUTPUT_NAME "coda-idl"
        PREFIX "")
      
      install(TARGETS coda_idl DESTINATION ${IDL_INSTALL_DIR})
  
      configure_file(
        ${CMAKE_CURRENT_SOURCE_DIR}/idl/coda-idl.dlm.in
        ${CMAKE_CURRENT_BINARY_DIR}/idl/coda-idl.dlm)
      
      install(
        FILES ${CMAKE_CURRENT_BINARY_DIR}/idl/coda-idl.dlm 
        DESTINATION ${IDL_INSTALL_DIR})
  
    else(IDL_DIR)
      message(FATAL_ERROR "IDL not found. Try setting the IDL_DIR environment or CMake variable to the root directory of an IDL installation.")
    endif(IDL_DIR)
  
  endif(CODA_BUILD_IDL)
  
  
  # matlab
  
  if(CODA_BUILD_MATLAB)
  
    # If not in cache, try from environment
    if(NOT MATLAB_DIR)
      if($ENV{MATLAB_DIR} MATCHES ".+")
        file(TO_CMAKE_PATH $ENV{MATLAB_DIR} MATLAB_DIR)
      endif($ENV{MATLAB_DIR} MATCHES ".+")
    endif(NOT MATLAB_DIR)
    
    if(MATLAB_DIR)
  
      file(TO_CMAKE_PATH ${MATLAB_DIR} MATLAB_DIR)
  
      if(NOT EXISTS "${MATLAB_DIR}")
        message(FATAL_ERROR "Specified MATLAB directory '${MATLAB_DIR}' does not exist")
      endif(NOT EXISTS "${MATLAB_DIR}")
  
      if(NOT IS_DIRECTORY "${MATLAB_DIR}")
        message(FATAL_ERROR "Specified MATLAB directory '${MATLAB_DIR}' exists, but is not a directory")
      endif(NOT IS_DIRECTORY "${MATLAB_DIR}")
  
      set(MATLAB_DIR ${MATLAB_DIR} CACHE PATH "Root directory of MATLAB installation" FORCE)
      if(WIN32)
        set(MATLAB_INSTALL_DIR matlab)
      else()
        set(MATLAB_INSTALL_DIR lib/matlab)
      endif()
  
      find_path(MATLAB_INCLUDE_DIR "mex.h" ${MATLAB_DIR}/extern/include)
      mark_as_advanced(MATLAB_INCLUDE_DIR)
  
      set(CODA_MATLAB_SOURCE_FILES
        ${CODA_SOURCE_DIR}/matlab/coda-matlab-getdata.c
        ${CODA_SOURCE_DIR}/matlab/coda-matlab-traverse.c
        ${CODA_SOURCE_DIR}/matlab/coda_matlab.c)
  
      # Configure the mex compiler
      set(MEXFLAGS -I"${MATLAB_INCLUDE_DIR}" -I. -I"${CODA_BINARY_DIR}" -I"${CODA_SOURCE_DIR}/libcoda" -DHAVE_CONFIG_H)
      # Note: The explicit mex.bat assignment is necessary because
      # only with the .bat attached will nmake invoke the command
      # using 'call' -- and only that works if the path name
      # contains spaces (as in 'Program Files', the default for
      # MATLAB 2007 and beyond). So relying (as we did before) on
      # the fact that on Windows you can theoretically also invoke
      # a batchfile without the extension will fail in that case.
      if(WIN32)
        set(MEX "${MATLAB_DIR}/bin/mex.bat")
        set(MEXEXT "mexw32")
        set(MEXFLAGS ${MEXFLAGS} -DWIN32_LEAN_AND_MEAN -DWIN32 -DHAVE_CONFIG_H -v)
      else(WIN32)
        set(MEX "${MATLAB_DIR}/bin/mex")
        set(MEXEXT_PROG "mexext")
        set(MEXFLAGS ${MEXFLAGS} -g -O)
        file(TO_NATIVE_PATH "${MATLAB_DIR}/bin/${MEXEXT_PROG}" MEXEXT_CMD)
        execute_process(COMMAND "${MEXEXT_CMD}" OUTPUT_VARIABLE MEXEXT)
        string(STRIP ${MEXEXT} MEXEXT)
      endif(WIN32)
  
      set(MEX_OUTPUT ${CMAKE_CURRENT_BINARY_DIR}/coda_matlab.${MEXEXT})
  
      # Because we are essentially bypassing CMake with the mex command,
      # we need to specify exactly where the build libraries can be found.
      # Since the user may have overridden CMAKE_ARCHIVE_OUTPUT_DIRECTORY,
      # we have to take that possibility into account. Just prefixing is
      # not enough, because the variable can also be empty, and then you'd
      # get names starting with a slash.
      set(INT_LIB_DIR ${CMAKE_CURRENT_BINARY_DIR}/${CMAKE_ARCHIVE_OUTPUT_DIRECTORY})
  
      if(WIN32)
        set(MEX_CMD ${MEX} ${MEXFLAGS} ${CODA_MATLAB_SOURCE_FILES} ${INT_LIB_DIR}/libcoda.lib ${HDF4_LIBRARIES} ${HDF5_LIBRARIES} -output ${MEX_OUTPUT})
      else(WIN32)
        set(MEX_CMD ${MEX} ${MEXFLAGS} ${CODA_MATLAB_SOURCE_FILES} ${INT_LIB_DIR}/libcoda.a ${HDF4_LIBRARIES} ${HDF5_LIBRARIES} -o ${MEX_OUTPUT})
      endif(WIN32)
   
      add_custom_command(
        COMMENT "Compiling the CODA MATLAB interface..."
        DEPENDS ${CODA_MATLAB_SOURCE_FILES}
        OUTPUT ${MEX_OUTPUT}
        COMMAND ${MEX_CMD})
   
      add_custom_target(coda_matlab ALL DEPENDS ${MEX_OUTPUT})
  
      add_dependencies(coda_matlab coda_static)
      install(FILES ${MEX_OUTPUT} DESTINATION ${MATLAB_INSTALL_DIR})
  
      # *** matlab data ***
  
      set(CODA_MATLAB_MFILES
        matlab/coda_attributes.m
        matlab/coda_class.m
        matlab/coda_clearall.m
        matlab/coda_close.m
        matlab/coda_description.m
        matlab/coda_fetch.m
        matlab/coda_fieldavailable.m
        matlab/coda_fieldcount.m
        matlab/coda_fieldnames.m
        matlab/coda_getopt.m
        matlab/coda_open.m
        matlab/coda_open_as.m
        matlab/coda_options.m
        matlab/coda_param.m
        matlab/coda_product_class.m
        matlab/coda_product_type.m
        matlab/coda_product_version.m
        matlab/coda_setopt.m
        matlab/coda_size.m
        matlab/coda_time_to_string.m
        matlab/coda_unit.m
        matlab/coda_version.m)
      
      install(FILES ${CODA_MATLAB_MFILES} DESTINATION ${MATLAB_INSTALL_DIR})
      
    else(MATLAB_DIR)
      
      message(FATAL_ERROR "MATLAB not found. Try setting the MATLAB_DIR environment or CMake variable to the root directory of a MATLAB installation.")
      
    endif(MATLAB_DIR)
    
  endif(CODA_BUILD_MATLAB)
  
  
  # python
  
  if(CODA_BUILD_PYTHON)
  
    find_package(PythonInterp)
    if(NOT PYTHONINTERP_FOUND)
      message(FATAL_ERROR "PYTHON interpreter not found.")
    endif(NOT PYTHONINTERP_FOUND)
  
    find_package(PythonLibs)
    if(NOT PYTHONLIBS_FOUND)
      message(FATAL_ERROR "PYTHON libraries or include files not found.")
    endif(NOT PYTHONLIBS_FOUND)
    include_directories(${PYTHON_INCLUDE_DIRS})
  
    find_package(NumPy)
    if(NOT NUMPY_FOUND)
      message(FATAL_ERROR "Python numpy package is needed for the Python interface. Please install 'numpy' or, if you have already installed this package, set the NUMPY_INCLUDE environment variable and make sure that the file $NUMPY_INCLUDE/numpy/ndarrayobject.h exists.")
    endif(NOT NUMPY_FOUND)
    include_directories(${NUMPY_INCLUDE_DIR})
  
    execute_process(COMMAND ${PYTHON_EXECUTABLE} -c "import sys; from distutils import sysconfig; sys.stdout.write(sysconfig.get_python_lib(1,0,prefix=''))" OUTPUT_VARIABLE PYTHON_INSTALL_DIR)
    set(CODA_PYTHON_INSTALL_DIR "${PYTHON_INSTALL_DIR}" CACHE STRING "Install location for CODA Python package")
  
    add_library(coda_python MODULE python/codac.c)
    target_link_libraries(coda_python coda_static ${HDF4_LIBRARIES} ${HDF5_LIBRARIES} ${PYTHON_LIBRARIES})
    set_target_properties(coda_python
      PROPERTIES OUTPUT_NAME "_codac"
      PREFIX "")
    if(WIN32)
      set_target_properties(coda_python PROPERTIES SUFFIX .pyd)
    endif()
  
    install(TARGETS coda_python DESTINATION ${CODA_PYTHON_INSTALL_DIR}/coda)
    install(FILES
      python/__init__.py
      python/codac.py
      python/codapython.py
      DESTINATION ${CODA_PYTHON_INSTALL_DIR}/coda)
  
  endif(CODA_BUILD_PYTHON)
  
endif(NOT CODA_BUILD_SUBPACKAGE_MODE)

# Custom CODA expat library
#
set(LIBEXPAT_SOURCES
  libcoda/expat/ascii.h
  libcoda/expat/asciitab.h
  libcoda/expat/coda_expat_mangle.h
  libcoda/expat/expat.h
  libcoda/expat/expat_external.h
  libcoda/expat/iasciitab.h
  libcoda/expat/internal.h
  libcoda/expat/latin1tab.h
  libcoda/expat/nametab.h
  libcoda/expat/utf8tab.h
  libcoda/expat/xmlparse.c
  libcoda/expat/xmlrole.c
  libcoda/expat/xmlrole.h
  libcoda/expat/xmltok.c
  libcoda/expat/xmltok.h
  libcoda/expat/xmltok_impl.h)

include_directories(BEFORE ${CMAKE_CURRENT_SOURCE_DIR}/libcoda/expat)


# Custom CODA pcre library
#
set(LIBPCRE_SOURCES
  libcoda/pcre/coda_pcre_config.h
  libcoda/pcre/coda_pcre_mangle.h
  libcoda/pcre/pcre.h
  libcoda/pcre/pcre_byte_order.c
  libcoda/pcre/pcre_chartables.c
  libcoda/pcre/pcre_compile.c
  libcoda/pcre/pcre_config.c
  libcoda/pcre/pcre_dfa_exec.c
  libcoda/pcre/pcre_exec.c
  libcoda/pcre/pcre_fullinfo.c
  libcoda/pcre/pcre_get.c
  libcoda/pcre/pcre_globals.c
  libcoda/pcre/pcre_internal.h
  libcoda/pcre/pcre_maketables.c
  libcoda/pcre/pcre_newline.c
  libcoda/pcre/pcre_ord2utf8.c
  libcoda/pcre/pcre_printint.c
  libcoda/pcre/pcre_refcount.c
  libcoda/pcre/pcre_string_utils.c
  libcoda/pcre/pcre_study.c
  libcoda/pcre/pcre_tables.c
  libcoda/pcre/pcre_ucd.c
  libcoda/pcre/pcre_valid_utf8.c
  libcoda/pcre/pcre_version.c
  libcoda/pcre/pcre_xclass.c
  libcoda/pcre/ucp.h)

include_directories(BEFORE ${CMAKE_CURRENT_SOURCE_DIR}/libcoda/pcre)

# Custom CODA zlib library
#
set(LIBZLIB_SOURCES
  libcoda/zlib/adler32.c
  libcoda/zlib/coda_zlib_mangle.h
  libcoda/zlib/crc32.c
  libcoda/zlib/crc32.h
  libcoda/zlib/gzguts.h
  libcoda/zlib/inffast.c
  libcoda/zlib/inffast.h
  libcoda/zlib/inffixed.h
  libcoda/zlib/inflate.c
  libcoda/zlib/inflate.h
  libcoda/zlib/inftrees.c
  libcoda/zlib/inftrees.h
  libcoda/zlib/zconf.h
  libcoda/zlib/zlib.h
  libcoda/zlib/zutil.c
  libcoda/zlib/zutil.h)

include_directories(BEFORE ${CMAKE_CURRENT_SOURCE_DIR}/libcoda/zlib)

if(NOT CODA_BUILD_SUBPACKAGE_MODE)
  install(FILES ${CMAKE_CURRENT_BINARY_DIR}/coda.h DESTINATION include)
endif(NOT CODA_BUILD_SUBPACKAGE_MODE)

if(NOT CODA_BUILD_SUBPACKAGE_MODE)
  #
  # Set dynamic library version
  #
  set(LIBCODA_CURRENT 14)
  set(LIBCODA_REVISION 3)
  set(LIBCODA_AGE 0)
  math(EXPR LIBCODA_MAJOR "${LIBCODA_CURRENT} - ${LIBCODA_AGE}")
  set(LIBCODA_MINOR ${LIBCODA_AGE})
  
  add_library(coda SHARED ${LIBCODA_SOURCES} ${LIBEXPAT_SOURCES} ${LIBPCRE_SOURCES} ${LIBZLIB_SOURCES})
  target_link_libraries(coda ${HDF4_LIBRARIES} ${HDF5_LIBRARIES})
  set_target_properties(coda PROPERTIES
    VERSION ${LIBCODA_MAJOR}.${LIBCODA_MINOR}.${LIBCODA_REVISION}
    SOVERSION ${LIBCODA_MAJOR})
  if(WIN32)
    if(CODA_WITH_HDF5)
      # Also set H5_BUILT_AS_DYNAMIC_LIB since we link against the dynamic HDF5 library
      set_target_properties(coda PROPERTIES COMPILE_FLAGS "-DH5_BUILT_AS_DYNAMIC_LIB -DXML_BUILDING_EXPAT -DLIBCODADLL -DLIBCODADLL_EXPORTS -DYY_NO_UNISTD_H")
    else(CODA_WITH_HDF5)
      set_target_properties(coda PROPERTIES COMPILE_FLAGS "-DXML_BUILDING_EXPAT -DLIBCODADLL -DLIBCODADLL_EXPORTS -DYY_NO_UNISTD_H")
    endif(CODA_WITH_HDF5)
  endif(WIN32)
  install(TARGETS coda DESTINATION lib)
endif(NOT CODA_BUILD_SUBPACKAGE_MODE)

add_library(coda_static STATIC ${LIBCODA_SOURCES} ${LIBEXPAT_SOURCES} ${LIBPCRE_SOURCES} ${LIBZLIB_SOURCES})

# On Windows, we want libcoda.lib for static, coda.dll & coda.lib for shared.
# On Unix, we want libcoda.a and libcoda.so
if(WIN32)
  # Also set XML_STATIC on Windows so expat is build for inclusion into a static library
  set_target_properties(coda_static PROPERTIES OUTPUT_NAME "libcoda")
  if(CODA_WITH_HDF5)
    # Also set H5_BUILT_AS_DYNAMIC_LIB since we link against the dynamic HDF5 library
    set_target_properties(coda_static PROPERTIES COMPILE_FLAGS "-DH5_BUILT_AS_DYNAMIC_LIB -DXML_BUILDING_EXPAT -DYY_NO_UNISTD_H")
  else(CODA_WITH_HDF5)
    set_target_properties(coda_static PROPERTIES COMPILE_FLAGS "-DXML_BUILDING_EXPAT -DYY_NO_UNISTD_H")
  endif(CODA_WITH_HDF5)
else(WIN32)
  set_target_properties(coda_static PROPERTIES OUTPUT_NAME "coda")
endif(WIN32)
if(NOT CODA_BUILD_SUBPACKAGE_MODE)
  install(TARGETS coda_static DESTINATION lib)
endif(NOT CODA_BUILD_SUBPACKAGE_MODE)

if(MSVC)
  set_source_files_properties(coda-expr-tokenizer.c PROPERTIES
    COMPILE_FLAGS "/D YY_NO_UNISTD_H")
endif(MSVC)


if(NOT CODA_BUILD_SUBPACKAGE_MODE)
  # tools codacheck
  
  set(codacheck_SOURCES tools/codacheck/codacheck.c)
  add_executable(codacheck ${codacheck_SOURCES})
  target_link_libraries(codacheck coda_static ${HDF4_LIBRARIES} ${HDF5_LIBRARIES} ${LIBM_LIBRARY})
  install(TARGETS codacheck DESTINATION bin)
  
  if(MSVC)
    set_target_properties(codacheck PROPERTIES
      COMPILE_FLAGS "/nologo /GX /D_CONSOLE /D_MBCS /YX /FD")
  endif(MSVC)
  
  # tools codacmp
  
  set(codacmp_SOURCES tools/codacmp/codacmp.c)
  add_executable(codacmp ${codacmp_SOURCES})
  target_link_libraries(codacmp coda_static ${HDF4_LIBRARIES} ${HDF5_LIBRARIES} ${LIBM_LIBRARY})
  install(TARGETS codacmp DESTINATION bin)
  
  # tools/codadump
  
  set(codadump_SOURCES
      tools/codadump/codadump-ascii.c
      tools/codadump/codadump-debug.c
      tools/codadump/codadump-dim.c
      tools/codadump/codadump-filter.c
      tools/codadump/codadump-json.c
      tools/codadump/codadump-traverse.c
      tools/codadump/codadump-yaml.c
      tools/codadump/codadump.c
      tools/codadump/codadump-filter.h
      tools/codadump/codadump.h)
  set(codadump_hdf4_files tools/codadump/codadump-hdf4.c)
  if(CODA_WITH_HDF4)
    set(codadump_SOURCES ${codadump_SOURCES} ${codadump_hdf4_files})
  endif(CODA_WITH_HDF4)
  add_executable(codadump ${codadump_SOURCES})
  target_link_libraries(codadump coda_static ${HDF4_LIBRARIES} ${HDF5_LIBRARIES} ${LIBM_LIBRARY})
  install(TARGETS codadump DESTINATION bin)
  
  # tools codaeval
  
  set(codaeval_SOURCES tools/codaeval/codaeval.c)
  add_executable(codaeval ${codaeval_SOURCES})
  target_link_libraries(codaeval coda_static ${HDF4_LIBRARIES} ${HDF5_LIBRARIES} ${LIBM_LIBRARY})
  install(TARGETS codaeval DESTINATION bin)
  
  # tools codafind
  
  set(codafind_SOURCES tools/codafind/codafind.c)
  add_executable(codafind ${codafind_SOURCES})
  target_link_libraries(codafind coda_static ${HDF4_LIBRARIES} ${HDF5_LIBRARIES} ${LIBM_LIBRARY})
  install(TARGETS codafind DESTINATION bin)
endif(NOT CODA_BUILD_SUBPACKAGE_MODE)

# tools codadd

set(codadd_SOURCES
  tools/codadd/codadd.c
  tools/codadd/codadd-definition.c
  tools/codadd/codadd-detect.c
  tools/codadd/codadd-doc.c
  tools/codadd/codadd-list.c
  tools/codadd/codadd-xmlschema.c)
add_executable(codadd ${codadd_SOURCES})
target_link_libraries(codadd coda_static ${HDF4_LIBRARIES} ${HDF5_LIBRARIES} ${LIBM_LIBRARY})
if(NOT CODA_BUILD_SUBPACKAGE_MODE)
  install(TARGETS codadd DESTINATION bin)
endif(NOT CODA_BUILD_SUBPACKAGE_MODE)


# config files
#
add_definitions(-DHAVE_CONFIG_H)

configure_file(
  ${CMAKE_CURRENT_SOURCE_DIR}/config.h.cmake.in
  ${CMAKE_CURRENT_BINARY_DIR}/config.h)
configure_file(
  ${CMAKE_CURRENT_SOURCE_DIR}/libcoda/coda.h.cmake.in
  ${CMAKE_CURRENT_BINARY_DIR}/coda.h)


if(NOT CODA_BUILD_SUBPACKAGE_MODE)
  # Windows specific build rules
  
  # Notice that doing this on Linux does not copy the
  # DOS line endings (CMake bug 0008506)
  #
  configure_file(
    ${CMAKE_CURRENT_SOURCE_DIR}/windows/codawin32.iss.in
    ${CMAKE_CURRENT_BINARY_DIR}/windows/codawin32.iss)
  configure_file(
    ${CMAKE_CURRENT_SOURCE_DIR}/windows/codawin64.iss.in
    ${CMAKE_CURRENT_BINARY_DIR}/windows/codawin64.iss)
  configure_file(
    ${CMAKE_CURRENT_SOURCE_DIR}/windows/setup.py.in
    ${CMAKE_CURRENT_BINARY_DIR}/windows/setup.py)
endif(NOT CODA_BUILD_SUBPACKAGE_MODE)
