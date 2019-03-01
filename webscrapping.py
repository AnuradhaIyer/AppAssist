from multiprocessing import Process

import requests
import pandas as pd
from tqdm import tqdm
import re

headers = {
    'User-Agent': 'Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/42.0.2311.90 '
                  'Safari/537.36'}

directory = './scrape/{}.txt'


def scrape(asin):
    page = requests.get('http://www.amazon.com/dp/{}'.format(asin), headers=headers)
    contents = str(page.content)
    f = open(directory.format(asin), "w+")
    f.write(contents)
    f.close()


def parseresults(asin):
    f = open(directory.format(asin), "r")
    contents = f.read()
    f.close()
    m = re.search('<meta name="title" content="Amazon.com:(.+?):', contents)
    if m:
        title = m.group(1)
        title = title.replace(',','').strip()
    else:
        return None, None, None
    m = re.search('data-a-dynamic-image="{(.+?)}', contents)
    if m:
        img_src_raw = m.group(1)
        processed = [img_src for img_src in img_src_raw.split('&quot;') if 'https://' in img_src]
        processed = " ".join(processed)
    else:
        processed = ''
    m = re.search('<h2>Product description</h2>(.+?) </div>', contents)
    #     print(m)
    if m:
        app_description = m.group(1)
        app_description = app_description.replace('\\n        <div class="a-row masrw-content-row">\\n', ' ').strip()
        app_description = app_description.replace('\\n', '').strip()
        app_description = app_description.replace('<br />', '').strip()
        app_description = app_description.replace(',',' ').strip()

    else:
        app_description = ""
    return title, processed, app_description


import os.path

if __name__ == '__main__':
    df = pd.read_csv('outfile_.csv')
    items = df.asin.values
    for i in tqdm(range(0, len(items), 100)):
        if i + 100 > len(items):
            _end = len(items)
        else:
            _end = i + 100
        _items = items[i: _end]
        processes = []
        for i in range(0, len(_items)):
            if os.path.exists(directory.format(_items[i])):
                continue
            processes.append(Process(target=scrape, args=[_items[i]]))
            processes[-1].start()
        for i in range(len(processes)):
            processes[i].join()
    print('Finished fetching files')
    f = open("app_information.csv", "a+")
    f.write('asin,brand,category,desc_arr,imUrl\n')
    f.close()
    items_categories = df.categories.values
    for i in tqdm(range(0, len(items))):
        name, img_src, desc = parseresults(items[i])

        if name is not None:
            f = open("app_information.csv", "a+")
            f.write("{},{},{},{},{}\n".format(items[i], name, (items_categories[i]).replace(',', ' '), desc, img_src))
            f.close()
    print('Done scrapping!!\n Now Cleaning up the ratings file.')
    df = pd.read_csv('app_information.csv')
    asin = df.asin.values
    print('unique items {}.'.format(df.asin.nunique()))
    data_df = pd.read_csv('ratings_Apps_for_Android.csv', header=None)
    data_df.columns = ['user', 'item', 'rating', 'timestamp']
    data_df = data_df.drop('timestamp', axis=1)
    newdf = data_df.loc[data_df['item'].isin(asin)]
    # newdf = newdf.reset_index()
    print(newdf.columns)
    newdf.to_csv('ratings_Apps_for_Android_processed.csv',index = False)
    print(newdf.shape)
