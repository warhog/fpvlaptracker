#!/usr/bin/python

import sys
import tkinter as tk
import serial                  
import io    
import _thread
import time


class MyFirstGUI:
    def __init__(self, master):
        self.master = master
        self.variables = {}
        self.running = True
        master.title("fpvlaptracker tracer")
        master.width = 1024
        master.height = 768

#        self.close_button = tk.Button(master, text="close", command=master.quit)
#        self.close_button.pack()
        
        self.state = tk.Label(master, text="state: ")
        self.state.pack()
        
        self.varlabel = tk.Message(master, text="", width = 800)
        self.varlabel.pack()

        self.clear_button = tk.Button(master, text="clear log", command=self.clear)
        self.clear_button.pack()

        self.start_button = tk.Button(master, text="start", command=self.start)
        self.start_button.pack()
        self.stop_button = tk.Button(master, text="stop", command=self.stop)
        self.stop_button.pack()

        self.text_log = tk.Text(master, height = 20, width = 80)
        self.text_log.pack()

    def addlog(self, text):
        if self.running:
            self.text_log.mark_set(tk.INSERT, "1.0+%d chars" % 0)
            millis = int(round(time.time() * 1000))
            self.text_log.insert(tk.INSERT, str(millis) + ": " + text + "\n")
    
    def clear(self):
        self.text_log.delete("1.0", tk.END)
        
    def update(self):
#        print(self.variables)
        text = ""
        for var in self.variables:
            text = text + var + "=" + self.variables[var] + "\n"
        self.varlabel["text"] = text
        
    def setvariable(self, var, val):
        if self.running:
            self.variables[var] = val
            self.update()
        
    def setstate(self, val):
        if self.running:
            self.state["text"] = "state: " + val
        
    def start(self):
        self.running = True
        
    def stop(self):
        self.running = False
        
root = tk.Tk()
my_gui = MyFirstGUI(root)

UART = serial.Serial('/dev/ttyUSB0', 115200)
if not UART.isOpen():
    print("open uart port")
    UART.open()

if not UART.isOpen():
    print("failed to open port")
    sys.exit(0)
    

def read_serial():
    ser_io = io.TextIOWrapper(io.BufferedRWPair(UART, UART, 1), newline = '\r', line_buffering = True)
    while True:
        try:
            line_data = ser_io.readline().replace("\n", "").replace("\r", "");
            if line_data.startswith("STATE: "):
                my_gui.setstate(line_data.replace("STATE: ", ""))
            elif line_data.startswith("VAR: "):
                var, val = line_data.replace("VAR: ", "").split("=")
                my_gui.setvariable(var, val)
#                if line_data.startswith("VAR: rssi="):
#                    my_gui.addlog(line_data)
            else:
                my_gui.addlog(line_data)
        except UnicodeDecodeError:
            print("unicode error")
        
_thread.start_new_thread(read_serial, ())

root.mainloop()

