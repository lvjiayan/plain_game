import pygame
import random
import sys

# 调试代码：检查pygame模块的来源
print(f"Pygame module loaded from: {pygame.__file__}")

# 初始化Pygame
if pygame.init()[1] > 0:  # 检查是否有初始化错误
    print("Pygame initialization failed!")
    sys.exit()
else:
    print("Pygame initialized successfully.")

# 游戏窗口设置
try:
    WIDTH, HEIGHT = 800, 400
    screen = pygame.display.set_mode((WIDTH, HEIGHT))
    pygame.display.set_caption("Shooting Game")
    print("Game window created successfully.")
except Exception as e:
    print(f"Failed to create game window: {e}")
    sys.exit()

# 颜色定义
WHITE = (255, 255, 255)
BLACK = (0, 0, 0)
RED = (255, 0, 0)
BLUE = (0, 0, 255)

# 游戏变量
player_width, player_height = 50, 50
bullet_width, bullet_height = 10, 5
player_speed = 5
bullet_speed = 10
ai_move_interval = 30  # AI移动间隔帧数
max_player_bullets_per_second = 2
failure_count = 0
ai_hit_count = 0  # 新增变量：AI被击中的次数

# 新增变量：AI移动方向和持续时间
ai_direction = random.choice([-1, 1])  # -1表示向左，1表示向右
ai_direction_duration = 60  # 持续帧数
ai_direction_timer = 0

# 初始化玩家和AI
player = pygame.Rect(WIDTH // 2 - player_width // 2, HEIGHT - player_height - 10, player_width, player_height)
ai = pygame.Rect(WIDTH // 2 - player_width // 2, 10, player_width, player_height)
player_bullets = []
ai_bullets = []

# 时钟和计时器
clock = pygame.time.Clock()
ai_move_timer = 0
player_last_shot_time = 0

# 字体设置
font = pygame.font.Font(None, 36)

def reset_game():
    global player, ai, player_bullets, ai_bullets
    player.x = WIDTH // 2 - player_width // 2
    player.y = HEIGHT - player_height - 10
    ai.x = WIDTH // 2 - player_width // 2
    ai.y = 10
    player_bullets.clear()
    ai_bullets.clear()

def draw_objects():
    screen.fill(WHITE)
    pygame.draw.rect(screen, BLUE, player)
    pygame.draw.rect(screen, RED, ai)
    for bullet in player_bullets:
        pygame.draw.rect(screen, BLUE, bullet)
    for bullet in ai_bullets:
        pygame.draw.rect(screen, RED, bullet)
    failure_text = font.render(f"Failures: {failure_count}", True, BLACK)
    screen.blit(failure_text, (10, 10))
    ai_hit_text = font.render(f"AI Hits: {ai_hit_count}", True, BLACK)  # 显示AI被击中的次数
    screen.blit(ai_hit_text, (WIDTH - 150, 10))
    pygame.display.flip()

def handle_player_input(keys):
    if keys[pygame.K_LEFT] and player.left > 0:
        player.x -= player_speed
    if keys[pygame.K_RIGHT] and player.right < WIDTH:
        player.x += player_speed

def fire_player_bullet():
    global player_last_shot_time
    current_time = pygame.time.get_ticks()
    if current_time - player_last_shot_time >= 300:  # 减少发射间隔，允许连续发射
        bullet = pygame.Rect(player.centerx - bullet_width // 2, player.top - bullet_height, bullet_width, bullet_height)
        player_bullets.append(bullet)
        player_last_shot_time = current_time

def move_ai():
    global ai_move_timer
    ai_move_timer += 1

    if ai_move_timer >= ai_move_interval // 8:  # 降低移动更新频率
        ai_move_timer = 0
        # 根据距离调整AI移动速度
        distance_to_player = abs(player.centerx - ai.centerx)
        ai_move_step = max(4, min(distance_to_player // 20, player_speed * 2))  # 增大移动步幅

        # 如果AI方块与玩家方块的水平距离小于子弹宽度，则停止移动
        if distance_to_player > bullet_width:
            if player.centerx < ai.centerx and ai.left > 0:  # 玩家在AI左边，向左移动
                ai.x -= ai_move_step
            elif player.centerx > ai.centerx and ai.right < WIDTH:  # 玩家在AI右边，向右移动
                ai.x += ai_move_step

        # AI随机发射子弹
        if random.randint(0, 100) < 10:  # 降低子弹发射概率
            bullet = pygame.Rect(ai.centerx - bullet_width // 2, ai.bottom, bullet_width, bullet_height)
            ai_bullets.append(bullet)

def move_bullets():
    for bullet in player_bullets[:]:
        bullet.y -= bullet_speed
        if bullet.bottom < 0:
            player_bullets.remove(bullet)
    for bullet in ai_bullets[:]:
        bullet.y += bullet_speed
        if bullet.top > HEIGHT:
            ai_bullets.remove(bullet)

def check_collisions():
    global failure_count, ai_hit_count
    for bullet in ai_bullets[:]:
        if player.colliderect(bullet):
            failure_count += 1  # 增加玩家中弹计数
            ai_bullets.remove(bullet)  # 移除击中玩家的子弹
            break
    for bullet in player_bullets[:]:
        if ai.colliderect(bullet):
            ai_hit_count += 1  # 增加AI被击中的计数
            player_bullets.remove(bullet)  # 移除击中AI的子弹
            break

# 游戏主循环
while True:
    try:
        # print("Game loop running...")  # 添加调试信息
        for event in pygame.event.get():
            if event.type == pygame.QUIT:
                print("Quit event detected. Exiting game.")
                pygame.quit()
                sys.exit()
            if event.type == pygame.KEYDOWN:
                if event.key == pygame.K_ESCAPE:  # 按下ESC键退出游戏
                    print("ESC key pressed. Exiting game.")
                    pygame.quit()
                    sys.exit()
        
        # 持续检测空格键是否被按下
        keys = pygame.key.get_pressed()
        if keys[pygame.K_SPACE]:
            fire_player_bullet()
        handle_player_input(keys)
        move_ai()
        move_bullets()
        check_collisions()
        draw_objects()
        clock.tick(60)
    except Exception as e:
        print(f"An error occurred: {e}")
        pygame.quit()
        sys.exit()
