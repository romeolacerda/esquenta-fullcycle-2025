import { PartialType } from '@nestjs/mapped-types';
import { Roles } from '@prisma/client';
import { CreateUserDto } from './create-user.dto';

export class UpdateUserDto extends PartialType(CreateUserDto) {
    name: string
    email: string
    password: string
    role: Roles
}
