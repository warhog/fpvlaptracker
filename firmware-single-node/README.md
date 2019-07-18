# fpvlaptracker

this is the currently used hardware platform based on esp32 and rx5808.

## pictures

![3d printed case](https://raw.githubusercontent.com/warhog/fpvlaptracker/master/hardware-single-node/bottom.png)
![3d printed case opened](https://raw.githubusercontent.com/warhog/fpvlaptracker/master/hardware-single-node/top.png)
![3d printed case](https://raw.githubusercontent.com/warhog/fpvlaptracker/master/hardware-single-node/fpvlaptrackerunit.jpg)
![3d printed case opened](https://raw.githubusercontent.com/warhog/fpvlaptracker/master/hardware-single-node/fpvlaptrackerunit_open.jpg)

## schematics
the schematics and pcb are drawn using KiCAD5 (see schematics folder).

[pdf schematics](https://raw.githubusercontent.com/warhog/fpvlaptracker-hardware/master/fpvlaptracker32/schematics/schematic.pdf)

## bom (bill of material)

most of the pcb parts are found in the [bom csv file](https://github.com/warhog/fpvlaptracker-hardware/blob/master/fpvlaptracker32/fpvlaptracker32-bom.csv).

some additional parts are needed optionally:
* heatsink with 20x20 mm and heatsink glue for the rx5808
* 5v power supply board if you want to power from a LiPo, ..., i used the [pololu D24V10F5](https://www.pololu.com/product/2831) for this.

## housing

there is a 3d printable housing availbe in the housing folder. printing in pla with 0.2 mm layer height is enough. no supports needed.

## gerber files

if you want to make your own pcb you can use the gerber files in the gerber folder. i've used itead and elecrow and both were fine (itead is a bit cheaper, elecrow was quicker).

## rx5808 5,8 GHz receiver mod
the rx5808 receiver needs to be modified to support the SPI protocol.
an excellent description by shea ivey can be found [here](https://github.com/sheaivey/rx5808-pro-diversity/blob/master/docs/rx5808-spi-mod.md).

