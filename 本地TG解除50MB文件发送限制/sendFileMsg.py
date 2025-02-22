import os
import asyncio
from telegram import Bot
from telegram.error import NetworkError

# 替换成你的Bot HTTP API
TOKEN = '754047254:AAFRtztLq0QdbcsQ-eUC4o99hMpbp_iBDF8'
# 替换成你的频道ID，一般是负数
GROUP_ID = '-1001914683223'
# 要发送的文件路径
FILE_PATH = r'E:\Work\xxx\xxx\xxx.apk'

# 使用本地代理的 URL
bot = Bot(token=TOKEN, base_url='http://127.0.0.1:8081/bot', base_file_url='http://127.0.0.1:8081/file/bot')

async def send_message(text):
    try:
        await bot.send_message(chat_id=GROUP_ID, text=text)
        print("消息发送成功!")
    except NetworkError as e:
        print(f"发送消息时出错: {e}")

async def send_file(file_path):
    try:
        with open(file_path, 'rb') as file:
            await bot.send_document(chat_id=GROUP_ID, document=file)
            print("文件发送成功!")
    except NetworkError as e:
        print(f"发送文件时出错: {e}")
    except Exception as e:
        print(f"发生错误: {e}")

async def main():
    # 发送文本消息
    # await send_message("你好，这是一个测试消息！")
    # 发送文件
    await send_file(FILE_PATH)

if __name__ == '__main__':
    asyncio.run(main())
