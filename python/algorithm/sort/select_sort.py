#!/usr/bin/env python
# -*- coding: utf-8 -*-


import tools


@tools.exectime
def select_sort(array):
    for i in range(len(array)):
        x = i
        for j in range(i, len(array)):
            if array[j] < array[x]:
                x = j
        array[i], array[x] = array[x], array[i]
    return array


def main():
    """选择排序

    （1）初始状态：无序区为R[1..n]，有序区为空；
    （2）第i趟排序(i=1,2,3…n-1)开始时，当前有序区和无序区分别为R[1..i-1]和R(i..n）。
        该趟排序从当前无序区中-选出关键字最小的记录 R[k]，将它与无序区的第1个记录R交换，
        使R[1..i]和R[i+1..n)分别变为记录个数增加1个的新有序区和记录个数减少1个的新无序区；
    （3）n-1趟结束，数组有序化了。

    :return:
    """

    array = tools.load_random_array()
    print select_sort(array) == sorted(array)


if __name__ == '__main__':
    main()
