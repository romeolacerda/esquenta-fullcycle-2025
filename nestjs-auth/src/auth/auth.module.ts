import { Global, Module } from '@nestjs/common';
import { JwtModule } from '@nestjs/jwt';
import { AuthController } from './auth.controller';
import { AuthService } from './auth.service';

@Module({
  imports: [JwtModule.register({
    global: true,
    secret: 'secret',
    signOptions: {
      expiresIn: "2h", algorithm: 'HS256'
    }
  })],
  controllers: [AuthController],
  providers: [AuthService],
})
export class AuthModule {}
