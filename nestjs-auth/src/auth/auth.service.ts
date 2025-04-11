import { packRules } from '@casl/ability/extra';
import { Injectable } from '@nestjs/common';
import { JwtService } from '@nestjs/jwt';
import bcrypt from 'bcrypt';
import { CaslAbilityService } from 'src/casl/casl-ability/casl-ability.service';
import { PrismaService } from 'src/prisma/prisma.service';
import { LoginDto } from './dto/login-auth.dto';

@Injectable()
export class AuthService {
  constructor(private jwtService: JwtService, private pirsmaService: PrismaService, private abilityService: CaslAbilityService){}


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

    const ability = this.abilityService.createForUser(user)
    const token = this.jwtService.sign({name: user.name, email: user.email, role: user.role, sub: user.id, permissions: packRules(ability.rules)})
    return {
      access_token: token,
    }
  }



}
