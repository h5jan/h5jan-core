

from pandas import (
    DataFrame, HDFStore
)
import pandas as pd
import numpy as np

rand = np.random.random_sample((50,3))
df = DataFrame(rand, columns=['A', 'B', 'C',])
store = HDFStore('pandas_frame_small.h5')
store.put('d1', df, format='table', data_columns=True)
store.close()