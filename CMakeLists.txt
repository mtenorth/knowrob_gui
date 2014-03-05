cmake_minimum_required(VERSION 2.4.6)
include($ENV{ROS_ROOT}/core/rosbuild/rosbuild.cmake)

rosbuild_init()

set(EXECUTABLE_OUTPUT_PATH ${PROJECT_SOURCE_DIR}/bin)
set(LIBRARY_OUTPUT_PATH ${PROJECT_SOURCE_DIR}/lib)

add_classpath(${PROJECT_SOURCE_DIR}/bin)
add_runtime_classpath(${PROJECT_SOURCE_DIR}/bin)

add_jar_dir(${PROJECT_SOURCE_DIR}/lib)

rosbuild_find_ros_package(jpl)
add_classpath(${jpl_PACKAGE_PATH}/bin)
add_runtime_classpath(${jpl_PACKAGE_PATH}/bin)

rosbuild_find_ros_package(rosjava_jni)
add_classpath(${rosjava_jni_PACKAGE_PATH}/bin)
add_runtime_classpath(${rosjava_jni_PACKAGE_PATH}/bin)

rosbuild_find_ros_package(knowrob_common)
add_classpath(${knowrob_common_PACKAGE_PATH}/bin)
add_runtime_classpath(${knowrob_common_PACKAGE_PATH}/bin)
add_classpath(${knowrob_common_PACKAGE_PATH}/lib/commons-lang-2.6.jar)
add_runtime_classpath(${knowrob_common_PACKAGE_PATH}/lib/commons-lang-2.6.jar)
add_classpath(${knowrob_common_PACKAGE_PATH}/lib/commons-net-3.0.1.jar)
add_runtime_classpath(${knowrob_common_PACKAGE_PATH}/lib/commons-net-3.0.1.jar)
add_classpath(${knowrob_common_PACKAGE_PATH}/lib/guava-14.0.jar)
add_runtime_classpath(${knowrob_common_PACKAGE_PATH}/lib/guava-14.0.jar)
add_classpath(${knowrob_common_PACKAGE_PATH}/lib/HermiT.jar)
add_runtime_classpath(${knowrob_common_PACKAGE_PATH}/lib/HermiT.jar)
add_classpath(${knowrob_common_PACKAGE_PATH}/lib/owlapi-bin.jar)
add_runtime_classpath(${knowrob_common_PACKAGE_PATH}/lib/owlapi-bin.jar)
add_classpath(${knowrob_common_PACKAGE_PATH}/lib/vecmath.jar)
add_runtime_classpath(${knowrob_common_PACKAGE_PATH}/lib/vecmath.jar)

add_classpath(${knowrob_common_PACKAGE_PATH}/lib/httpcomponents-client/commons-codec-1.4.jar)
add_runtime_classpath(${knowrob_common_PACKAGE_PATH}/lib/httpcomponents-client/commons-codec-1.4.jar)
add_classpath(${knowrob_common_PACKAGE_PATH}/lib/httpcomponents-client/commons-logging-1.1.1.jar)
add_runtime_classpath(${knowrob_common_PACKAGE_PATH}/lib/httpcomponents-client/commons-logging-1.1.1.jar)
add_classpath(${knowrob_common_PACKAGE_PATH}/lib/httpcomponents-client/httpclient-4.1.1.jar)
add_runtime_classpath(${knowrob_common_PACKAGE_PATH}/lib/httpcomponents-client/httpclient-4.1.1.jar)
add_classpath(${knowrob_common_PACKAGE_PATH}/lib/httpcomponents-client/httpclient-cache-4.1.1.jar)
add_runtime_classpath(${knowrob_common_PACKAGE_PATH}/lib/httpcomponents-client/httpclient-cache-4.1.1.jar)
add_classpath(${knowrob_common_PACKAGE_PATH}/lib/httpcomponents-client/httpcore-4.1.jar)
add_runtime_classpath(${knowrob_common_PACKAGE_PATH}/lib/httpcomponents-client/httpcore-4.1.jar)
add_classpath(${knowrob_common_PACKAGE_PATH}/lib/httpcomponents-client/httpmime-4.1.1.jar)
add_runtime_classpath(${knowrob_common_PACKAGE_PATH}/lib/httpcomponents-client/httpmime-4.1.1.jar)



rosbuild_find_ros_package(knowrob_cad_parser)
add_classpath(${knowrob_cad_parser_PACKAGE_PATH}/bin)
add_runtime_classpath(${knowrob_cad_parser_PACKAGE_PATH}/bin)


rosbuild_find_ros_package(mod_vis)
add_classpath(${mod_vis_PACKAGE_PATH}/bin)
add_runtime_classpath(${mod_vis_PACKAGE_PATH}/bin)
add_classpath(${mod_vis_PACKAGE_PATH}/lib/core.jar)
add_runtime_classpath(${mod_vis_PACKAGE_PATH}/lib/core.jar)
add_classpath(${mod_vis_PACKAGE_PATH}/lib/controlP5.jar)
add_runtime_classpath(${mod_vis_PACKAGE_PATH}/lib/controlP5.jar)
add_classpath(${mod_vis_PACKAGE_PATH}/lib/snakeyaml-1.9.jar)
add_runtime_classpath(${mod_vis_PACKAGE_PATH}/lib/snakeyaml-1.9.jar)


# rosbuild_find_ros_package(mod_semantic_map)
# add_classpath(${mod_semantic_map_PACKAGE_PATH}/bin)
# add_runtime_classpath(${mod_semantic_map_PACKAGE_PATH}/bin)


# rosbuild_find_ros_package(re_comm)
# add_classpath(${re_comm_PACKAGE_PATH}/bin)
# add_runtime_classpath(${re_comm_PACKAGE_PATH}/bin)


# rosbuild_find_ros_package(re_comm_core)
# # add_classpath(${re_comm_core_PACKAGE_PATH}/bin)
# # add_runtime_classpath(${re_comm_core_PACKAGE_PATH}/bin)
# add_classpath(${re_comm_core_PACKAGE_PATH}/lib/argo-2.8.jar)
# add_runtime_classpath(${re_comm_core_PACKAGE_PATH}/lib/argo-2.8.jar)
# 

# 
# add_classpath(${re_comm_core_PACKAGE_PATH}/lib/jgrapht/jgrapht-jdk1.6.jar)
# add_runtime_classpath(${re_comm_core_PACKAGE_PATH}/lib/jgrapht/jgrapht-jdk1.6.jar)


add_java_source_dir(${PROJECT_SOURCE_DIR}/src)


# rospack_add_java_executable(KnowRobGUI jp.atr.unr.pf.gui.KnowRobGuiMain)



