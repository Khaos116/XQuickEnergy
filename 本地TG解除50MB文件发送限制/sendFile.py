import os
import sys
import asyncio
from telegram import Bot
from telegram.error import NetworkError

# 替换成你的Bot HTTP API
TOKEN = '7554047254:AAFRtztLq0QdbcsQ-eUC4o99hMpbp_iBDF8'
# 替换成你的频道ID，一般是负数
GROUP_ID = '-1001914683223'

# 使用本地代理的 URL
# 增加超时时间为60秒（可以根据需要调整）
bot = Bot(token=TOKEN, base_url='http://127.0.0.1:8081/bot', base_file_url='http://127.0.0.1:8081/file/bot', timeout=60)

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

async def main(file_path):
    # 发送文件
    await send_file(file_path)

if __name__ == '__main__':
    if len(sys.argv) != 2:
        print("请提供 APK 文件路径")
        sys.exit(1)

    file_path = sys.argv[1]
    asyncio.run(main(file_path))