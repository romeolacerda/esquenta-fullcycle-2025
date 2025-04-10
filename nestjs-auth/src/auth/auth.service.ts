import { Injectable } from '@nestjs/common';
import { JwtService } from '@nestjs/jwt';
import { PrismaService } from 'src/prisma/prisma.service';
import { LoginDto } from './dto/login-auth.dto';
import bcrypt from 'bcrypt'

@Injectable()
export class AuthService {
  constructor(private jwtService: JwtService, private pirsmaService: PrismaService){}


  async login(loginDto: LoginDto){
    const user = await this.pirsmaService.user.findUnique({
      where: { email: loginDto.email }
    })

    if(!user){
      throw new Error('Invalid Credentials')
    }

    const isPasswordValid = bcrypt.compareSync(
      loginDto.password,
      user.password
    )

    if(!isPasswordValid){
      throw new Error('Invalid Credentials')
    }

    const token = this.jwtService.sign({name: user.name, email: user.email, role: user.role, sub: user.id})
    return {
      access_token: token,
    }
  }



}
