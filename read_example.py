'''
/*-
 *******************************************************************************
 * Copyright (c) 2019 Halliburton International, Inc.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
'''

# Example opening one of the test files 
# You need to run the tests first which 
# generates some data to read, written by the API.

from pandas import DataFrame
from numpy import ndarray
import h5py

class DataFrameReader():
    ''' 
    Read to read DataFrame.
    This class is released open source by h5jan, see license above.
    TODO put it on pypi
    '''
    
    def __init__(self):
        pass
    
    def read(self, filePath):
        '''
        Read a data frame written by h5jan
        '''
        
        with h5py.File(filePath, 'r') as hf:
            
            node = hf.get("/")
            path = str(node.attrs["path"].decode('UTF-8'))
            data = str(node.attrs["data"].decode('UTF-8'))
            
            node = hf.get(path);
            columns = node.attrs["column_names"].astype("str")
            
            np_data = hf.get(data)
            return DataFrame(np_data, columns=columns)

reader = DataFrameReader()
print(reader.read('test-scratch/write_example/lazy_data_frame-2d.h5'))
