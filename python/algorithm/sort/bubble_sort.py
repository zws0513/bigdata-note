#!/usr/bin/env python
# -*- coding: utf-8 -*-


import tools


@tools.exectime
def bubble_sort(array):
    for i in range(len(array) - 1):  # 每次冒泡确定一个最大值，那么n个数比较，只需要进行n-1次冒泡就行了。
        for j in range(len(array) - 1 - i):  # 比较的次数实际是在以递减的方式减少，每一轮循环就是将一个尚未排序的最大值进行了一次冒泡。
            if array[j] > array[j + 1]:
                array[j], array[j + 1] = array[j + 1], array[j]
    return array


def main():
    """冒泡排序

    （1）比较相邻的元素。如果第一个比第二个大，就交换它们两个；
    （2）对每一对相邻元素作同样的工作，从开始第一对到结尾的最后一对，这样在最后的元素应该会是最大的数；
    （3）针对所有的元素重复以上的步骤，除了最后一个；
    （4）重复步骤1~3，直到排序完成。

    :return:
    """

    # tools.dump_random_array()
    array = tools.load_random_array()
    print(bubble_sort(array) == sorted(array))


if __name__ == '__main__':
    main()
