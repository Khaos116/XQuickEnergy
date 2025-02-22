import os
import asyncio
from telegram import Bot, InputMediaPhoto, InputMediaVideo
from telegram.error import NetworkError

# 替换成你的Bot HTTP API
TOKEN = '754047254:AAFRtztLq0QdbcsQ-eUC4o99hMpbp_iBDF8'
# 替换成你的频道ID，一般是负数
GROUP_ID = '-1001914683223'


bot = Bot(token=TOKEN, base_url='http://127.0.0.1:8081/bot', base_file_url='http://127.0.0.1:8081/file/bot')

async def send_message(text):
    try:
        await bot.send_message(chat_id=GROUP_ID, text=text)
        print("消息发送成功!")
    except NetworkError as e:
        print(f"发送消息时出错: {e}")

async def main():
    # 发送文本消息
    await send_message("你好，这是一个测试消息！")

if __name__ == '__main__':
    asyncio.run(main())
