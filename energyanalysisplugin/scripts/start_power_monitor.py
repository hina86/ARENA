import Monsoon.HVPM as HVPM
import csv,datetime, time
import Monsoon.sampleEngine as sampleEngine
import Monsoon.Operations as op
import Monsoon.pmapi as pmapi
import numpy as np
import sys

import subprocess

def install(package):
    subprocess.check_call([sys.executable, "-m", "pip", "install", package])
    
def testHVPM(serialno,Protocol,STOP_TIME, CSV_FILE_FOR_WRITING_SAMPLES):
    
    HVMON = HVPM.Monsoon()
    HVMON.setup_usb(serialno,Protocol)
    
    print("HVPM Serial Number: " + repr(HVMON.getSerialNumber()))
    HVMON.fillStatusPacket()
    HVMON.setVout(4.2)
    HVengine = sampleEngine.SampleEngine(HVMON)
   
    #Turning on periodic console outputs.
    HVengine.ConsoleOutput(False)
    
    #Setting all channels enabled
    HVengine.enableChannel(sampleEngine.channels.MainCurrent)
    HVengine.enableChannel(sampleEngine.channels.MainVoltage)
    #HVengine.disableChannel(sampleEngine.channels.USBCurrent)
    #HVengine.disableChannel(sampleEngine.channels.USBVoltage)
    HVengine.disableChannel(sampleEngine.channels.AuxCurrent)
    HVengine.enableChannel(sampleEngine.channels.USBCurrent)
    HVengine.enableChannel(sampleEngine.channels.USBVoltage)
    #HVengine.enableChannel(sampleEngine.channels.AuxCurrent)
    HVengine.enableChannel(sampleEngine.channels.timeStamp)
    HVMON.setUSBPassthroughMode(op.USB_Passthrough.Auto)

    #Setting trigger conditions
    numSamples=sampleEngine.triggers.SAMPLECOUNT_INFINITE

    HVengine.setStartTrigger(sampleEngine.triggers.GREATER_THAN,0) 
    HVengine.setStopTrigger(sampleEngine.triggers.GREATER_THAN,STOP_TIME) # pass 85 as parameter from screen
    HVengine.setTriggerChannel(sampleEngine.channels.timeStamp) 
    #Setting trigger conditions
    #numSamples=5000*60
    
    #adb1('C:/Users/hina/AppData/Local/Android/android-sdks/platform-tools/dm.bat')

    #sampling start at this time
    t=gettime_ntp()
    
    #Actually start collecting samples
    #startSampling() continues until the trigger conditions have been met, and then ends automatically.
    HVengine.startSampling(numSamples)   # progress bar should show this smapling progress till the time runs out.
    
    #Get those samples as a Python list
    #Samples has the format  [[timestamp], [mainCurrent], [usbCurrent], [auxCurrent], [mainVolts],[usbVolts]]
    samples = HVengine.getSamples()
    
    #Perform analysis on the resulting data.  For example, in order to calculate, perform the following:
    #Current = samples[sampleEngine.channels.MainCurrent]
    #mainVoltage = samples[sampleEngine.channels.MainVoltage]
    
    #mainCurrent is given in mA.  Divide by 1000 to convert to Amps
    #scaledMainCurrent = [x / 1000 for x in Current]
    
    #Element-wise multiply to produce Watts. Power = Current * Voltage.  
    #mainPower = np.multiply(scaledMainCurrent, mainVoltage)
    with open(CSV_FILE_FOR_WRITING_SAMPLES, 'wb') as myfile:  #on screen ask for file name after settting and replace tht name
        wr = csv.writer(myfile, delimiter=',')
        wr.writerow(['sampling start at', t])
        #wr.writerow(['timeStamp', 'Current', 'mainVoltage', 'CurrentAmp', 'power','auxCurrent', 'usbCurrent','usbVoltage'])
        wr.writerow(['timeStamp', 'Current', 'mainVoltage'])
        
        for i in range(len(samples[sampleEngine.channels.timeStamp])):
            timeStamp =datetime.datetime.fromtimestamp((samples[sampleEngine.channels.timeStamp][i])+t).strftime('%Y-%m-%d %H:%M:%S.%f')
            #timeStamp = samples[sampleEngine.channels.timeStamp][i]
            Current = samples[sampleEngine.channels.MainCurrent][i]
            #auxCurrent = samples[sampleEngine.channels.AuxCurrent][i]
            #usbCurrent = samples[sampleEngine.channels.USBCurrent][i]
            mainVoltage = samples[sampleEngine.channels.MainVoltage][i]
            #usbVoltage = samples[sampleEngine.channels.USBVoltage][i]
            #CurrentAmp=scaledMainCurrent[i]
            #power_Watts=mainPower[i]
            wr.writerow([timeStamp, Current, mainVoltage])  # show on screen that wrting done and file saved. once the file is save then and only then run dm3.bat
            #wr.writerow([timeStamp, Current, mainVoltage, CurrentAmp, power_Watts])
            #wr.writerow([timeStamp, Current, mainVoltage, CurrentAmp, power, auxCurrent, usbCurrent,usbVoltage])
    time.sleep(3)
#--------------------------------------------------------

def adb1(filepath= ''):

    import subprocess
    from subprocess import Popen, PIPE
    p = subprocess.Popen(filepath,stdout=PIPE, stderr=PIPE)
    p.wait()
    stdout, stderr = p.communicate()
    #print stderr
    #print stdout
   # print p.returncode # is 0 if success


#----------------------------------------------------------

def adb2(filepath= ''):

    import subprocess
    from subprocess import Popen
    p = subprocess.Popen(filepath)
    #print p.returncode # is 0 if success
    
#------------------------------------------------------------
#time https://gist.github.com/guneysus/9f85ab77e1a11d0eebdb
#------------------------------------------------------------
def gettime_ntp(addr='1.ee.pool.ntp.org'):
    # http://code.activestate.com/recipes/117211-simple-very-sntp-client/
    import socket
    import struct
    import sys
    import time
    #TIME1970 = 2208988800L      # Thanks to F.Lundh
    client = socket.socket( socket.AF_INET, socket.SOCK_DGRAM )
    data = '\x1b' + 47 * '\0'
    client.sendto( data, (addr, 123))
    data, address = client.recvfrom( 1024 )
    if data:
        t = struct.unpack( '!12I', data )[10]
    #    t -= TIME1970
        #return time.ctime(t),t
        return t

#------------------------

def main(STOP_TIME, CSV_FILE_FOR_WRITING_SAMPLES):

    install('Monsoon')
    HVPMSerialNo = 21304
    testHVPM(HVPMSerialNo,pmapi.USB_protocol(),STOP_TIME,CSV_FILE_FOR_WRITING_SAMPLES)
    

#if __name__ == "__main__":
STOP_TIME= sys.argv[1]
CSV_FILE_FOR_WRITING_SAMPLES= sys.argv[2]
print ("a is:", STOP_TIME)
print ("b is:", CSV_FILE_FOR_WRITING_SAMPLES)

STOP_TIME= int (STOP_TIME)
#b= int(b)

main(STOP_TIME,CSV_FILE_FOR_WRITING_SAMPLES)
    

 
