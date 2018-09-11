#!/usr/bin/env python
# -*- coding: utf-8 -*-


def insert_sort(array):
    for i in range(len(array)):
        for j in range(i):
            if array[i] < array[j]:
                array.insert(j, array.pop(i))
                break
    return array


def main():

    print "插入排序"


if __name__ == '__main__':
    main()