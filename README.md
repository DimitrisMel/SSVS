# Single Update Sketch with Variable Counter Structure (SSVS)
Paper #206 submitted to Infocom 2023

In order to run this project, you will need a packet stream txt file in the format

Source IP | Destination IP
--- | --- 
123456789 | 987654321 
234567891 | 876543219
... | ...



You will also need a ground truth file in the format

Source IP | Destination IP | Real Flow Size
--- | --- | ---
123456789 | 987654321 | 101
234567891 | 876543219 | 8
... | ... | ...

where the Source IP and Destination IP combined, determine the flow ID.

Give the path of these two files to GeneralUtil.java in lines 19 and 20, respectively.

Finally, you need to give the path of the project to GeneralUtil.java in line 14.
