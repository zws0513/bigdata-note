#!/usr/bin/env python
# -*- coding: utf-8 -*-


from tools import exectime
import tools


@exectime
def bubble_sort(array):
    for i in range(len(array)):
        for j in range(i, len(array)):
            if array[i] > array[j]:
                array[i], array[j] = array[j], array[i]
    return array


def main():
    # tools.dump_random_array()
    array = tools.load_random_array()
    print(bubble_sort(array) == sorted(array))
    print "插入排序"


if __name__ == '__main__':
    main()
