#!/usr/bin/env python
# -*- coding: utf-8 -*-


import tools


@tools.exectime
def bubble_sort(array):
    for i in range(len(array)):
        for j in range(i, len(array)):
            if array[i] > array[j]:
                array[i], array[j] = array[j], array[i]
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
