#!/usr/bin/env python
import glob
import os
import shutil

path_pattern = "target/scala*/*.pom"
pom_files = glob.glob(path_pattern)
nr_of_poms = len(pom_files)

if nr_of_poms == 0:
  print ("Warning: Could not find pom file using path pattern: " + path_pattern)

elif nr_of_poms > 1:
  print ("Warning: Found multiple pom files " + str(nr_of_poms) + " using path pattern: " + path_pattern)

else:
  pom_file = pom_files[0]
  cur_dir = os.getcwd()
  print ("copying " + pom_file + " to " + cur_dir)
  shutil.copy(pom_file, cur_dir)

