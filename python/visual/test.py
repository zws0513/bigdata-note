#!/usr/bin/env python
# -*- coding: utf-8 -*-


import numpy as np
from matplotlib import pyplot as plt

x = np.arange(1, 11)
y = 2 * x + 5
plt.title("菜鸟教程 - 测试", fontproperties="SimSun")

plt.xlabel("x 轴", fontproperties="SimSun", fontsize=14)
plt.ylabel("y 轴", fontproperties="SimSun")
plt.plot(x, y)
plt.show()
