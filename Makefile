include $(shell rospack find mk)/cmake.mk

jar:
	cd bin && jar cf unr_roboearth_interface.jar * && cd ..

